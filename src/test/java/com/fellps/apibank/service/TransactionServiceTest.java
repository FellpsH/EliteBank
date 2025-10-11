package com.fellps.apibank.service;

import com.fellps.apibank.dto.DepositRequest;
import com.fellps.apibank.dto.TransactionResponse;
import com.fellps.apibank.dto.WithdrawRequest;
import com.fellps.apibank.enums.AccountType;
import com.fellps.apibank.enums.Role;
import com.fellps.apibank.enums.TransactionType;
import com.fellps.apibank.exception.BusinessException;
import com.fellps.apibank.exception.InsufficientBalanceException;
import com.fellps.apibank.model.Account;
import com.fellps.apibank.model.Transaction;
import com.fellps.apibank.model.User;
import com.fellps.apibank.repository.AccountRepository;
import com.fellps.apibank.repository.TransactionRepository;
import com.fellps.apibank.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private TransactionService transactionService;

    private User user;
    private Account account;
    private Authentication authentication;
    private SecurityContext securityContext;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("João Silva")
                .email("joao@email.com")
                .cpf("12345678901")
                .password("password")
                .role(Role.USER)
                .active(true)
                .build();

        account = Account.builder()
                .id(1L)
                .accountNumber("00000001-1")
                .agency("0001")
                .accountType(AccountType.CORRENTE)
                .balance(BigDecimal.valueOf(1000.00))
                .active(true)
                .user(user)
                .build();

        authentication = mock(Authentication.class);
        securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(user.getEmail());
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    }

    @Test
    @DisplayName("Deve realizar depósito com sucesso")
    void shouldDepositSuccessfully() {
        // Arrange
        DepositRequest request = DepositRequest.builder()
                .amount(BigDecimal.valueOf(500.00))
                .description("Depósito teste")
                .build();

        when(accountRepository.findById(anyLong())).thenReturn(Optional.of(account));
        
        Transaction savedTransaction = Transaction.builder()
                .id(1L)
                .transactionType(TransactionType.DEPOSITO)
                .amount(request.getAmount())
                .description(request.getDescription())
                .account(account)
                .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        // Act
        TransactionResponse response = transactionService.deposit(1L, request);

        // Assert
        assertNotNull(response);
        assertEquals(TransactionType.DEPOSITO, response.getTransactionType());
        assertEquals(request.getAmount(), response.getAmount());
        assertEquals(BigDecimal.valueOf(1500.00), account.getBalance());

        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(accountRepository, times(1)).save(any(Account.class));
        verify(auditService, times(1)).logTransaction(any(Transaction.class), any(User.class));
    }

    @Test
    @DisplayName("Deve realizar saque com sucesso")
    void shouldWithdrawSuccessfully() {
        // Arrange
        WithdrawRequest request = WithdrawRequest.builder()
                .amount(BigDecimal.valueOf(300.00))
                .description("Saque teste")
                .build();

        when(accountRepository.findById(anyLong())).thenReturn(Optional.of(account));
        
        Transaction savedTransaction = Transaction.builder()
                .id(1L)
                .transactionType(TransactionType.SAQUE)
                .amount(request.getAmount())
                .description(request.getDescription())
                .account(account)
                .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        // Act
        TransactionResponse response = transactionService.withdraw(1L, request);

        // Assert
        assertNotNull(response);
        assertEquals(TransactionType.SAQUE, response.getTransactionType());
        assertEquals(request.getAmount(), response.getAmount());
        assertEquals(BigDecimal.valueOf(700.00), account.getBalance());

        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    @DisplayName("Não deve realizar saque com saldo insuficiente")
    void shouldNotWithdrawWithInsufficientBalance() {
        // Arrange
        WithdrawRequest request = WithdrawRequest.builder()
                .amount(BigDecimal.valueOf(2000.00))
                .description("Saque teste")
                .build();

        when(accountRepository.findById(anyLong())).thenReturn(Optional.of(account));

        // Act & Assert
        InsufficientBalanceException exception = assertThrows(InsufficientBalanceException.class, () -> {
            transactionService.withdraw(1L, request);
        });

        assertEquals("Saldo insuficiente", exception.getMessage());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Não deve realizar operação em conta inativa")
    void shouldNotOperateOnInactiveAccount() {
        // Arrange
        account.setActive(false);
        
        DepositRequest request = DepositRequest.builder()
                .amount(BigDecimal.valueOf(100.00))
                .build();

        when(accountRepository.findById(anyLong())).thenReturn(Optional.of(account));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            transactionService.deposit(1L, request);
        });

        assertEquals("Conta inativa", exception.getMessage());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Não deve acessar conta de outro usuário")
    void shouldNotAccessOtherUserAccount() {
        // Arrange
        User anotherUser = User.builder()
                .id(2L)
                .name("Maria")
                .email("maria@email.com")
                .build();

        account.setUser(anotherUser);

        DepositRequest request = DepositRequest.builder()
                .amount(BigDecimal.valueOf(100.00))
                .build();

        when(accountRepository.findById(anyLong())).thenReturn(Optional.of(account));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            transactionService.deposit(1L, request);
        });

        assertEquals("Conta não pertence ao usuário", exception.getMessage());
    }
}

