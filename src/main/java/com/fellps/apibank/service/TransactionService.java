package com.fellps.apibank.service;

import com.fellps.apibank.dto.*;
import com.fellps.apibank.enums.TransactionType;
import com.fellps.apibank.exception.BusinessException;
import com.fellps.apibank.exception.InsufficientBalanceException;
import com.fellps.apibank.model.Account;
import com.fellps.apibank.model.Transaction;
import com.fellps.apibank.model.User;
import com.fellps.apibank.repository.AccountRepository;
import com.fellps.apibank.repository.TransactionRepository;
import com.fellps.apibank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional
    public TransactionResponse deposit(Long accountId, DepositRequest request) {
        log.info("Processing deposit of {} to account {}", request.getAmount(), accountId);

        User currentUser = getCurrentUser();
        Account account = getAccountAndValidateOwnership(accountId, currentUser);

        // Validar conta ativa
        if (!account.getActive()) {
            throw new BusinessException("Conta inativa");
        }

        // Realizar depósito
        account.deposit(request.getAmount());

        // Criar transação
        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.DEPOSITO)
                .amount(request.getAmount())
                .description(request.getDescription() != null ? request.getDescription() : "Depósito")
                .transactionDate(LocalDateTime.now())
                .account(account)
                .reversed(false)
                .build();

        transaction = transactionRepository.save(transaction);
        accountRepository.save(account);

        // Auditoria
        auditService.logTransaction(transaction, currentUser);

        log.info("Deposit successful. Transaction ID: {}", transaction.getId());

        return mapToTransactionResponse(transaction);
    }

    @Transactional
    public TransactionResponse withdraw(Long accountId, WithdrawRequest request) {
        log.info("Processing withdrawal of {} from account {}", request.getAmount(), accountId);

        User currentUser = getCurrentUser();
        Account account = getAccountAndValidateOwnership(accountId, currentUser);

        // Validar conta ativa
        if (!account.getActive()) {
            throw new BusinessException("Conta inativa");
        }

        // Validar saldo
        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Saldo insuficiente");
        }

        // Realizar saque
        account.withdraw(request.getAmount());

        // Criar transação
        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.SAQUE)
                .amount(request.getAmount())
                .description(request.getDescription() != null ? request.getDescription() : "Saque")
                .transactionDate(LocalDateTime.now())
                .account(account)
                .reversed(false)
                .build();

        transaction = transactionRepository.save(transaction);
        accountRepository.save(account);

        // Auditoria
        auditService.logTransaction(transaction, currentUser);

        log.info("Withdrawal successful. Transaction ID: {}", transaction.getId());

        return mapToTransactionResponse(transaction);
    }

    @Transactional
    public TransactionResponse transfer(Long accountId, TransferRequest request) {
        log.info("Processing transfer of {} from account {} to account {}",
                request.getAmount(), accountId, request.getTargetAccountNumber());

        User currentUser = getCurrentUser();
        Account sourceAccount = getAccountAndValidateOwnership(accountId, currentUser);

        // Validar conta origem ativa
        if (!sourceAccount.getActive()) {
            throw new BusinessException("Conta origem inativa");
        }

        // Buscar conta destino
        Account targetAccount = accountRepository.findByAccountNumber(request.getTargetAccountNumber())
                .orElseThrow(() -> new BusinessException("Conta destino não encontrada"));

        // Validar conta destino ativa
        if (!targetAccount.getActive()) {
            throw new BusinessException("Conta destino inativa");
        }

        // Validar transferência para a mesma conta
        if (sourceAccount.getId().equals(targetAccount.getId())) {
            throw new BusinessException("Não é possível transferir para a mesma conta");
        }

        // Validar saldo
        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Saldo insuficiente");
        }

        // Realizar transferência
        sourceAccount.withdraw(request.getAmount());
        targetAccount.deposit(request.getAmount());

        // Criar transação de saída
        Transaction outgoingTransaction = Transaction.builder()
                .transactionType(TransactionType.TRANSFERENCIA_ENVIADA)
                .amount(request.getAmount())
                .description(request.getDescription() != null ? request.getDescription() : "Transferência enviada")
                .transactionDate(LocalDateTime.now())
                .account(sourceAccount)
                .targetAccount(targetAccount)
                .reversed(false)
                .build();

        // Criar transação de entrada
        Transaction incomingTransaction = Transaction.builder()
                .transactionType(TransactionType.TRANSFERENCIA_RECEBIDA)
                .amount(request.getAmount())
                .description(request.getDescription() != null ? request.getDescription() : "Transferência recebida")
                .transactionDate(LocalDateTime.now())
                .account(targetAccount)
                .targetAccount(sourceAccount)
                .reversed(false)
                .build();

        transactionRepository.save(outgoingTransaction);
        transactionRepository.save(incomingTransaction);
        accountRepository.save(sourceAccount);
        accountRepository.save(targetAccount);

        // Auditoria
        auditService.logTransaction(outgoingTransaction, currentUser);

        log.info("Transfer successful. Transaction ID: {}", outgoingTransaction.getId());

        return mapToTransactionResponse(outgoingTransaction);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getExtract(Long accountId, Pageable pageable) {
        User currentUser = getCurrentUser();
        Account account = getAccountAndValidateOwnership(accountId, currentUser);

        Page<Transaction> transactions = transactionRepository.findByAccountId(account.getId(), pageable);

        return transactions.map(this::mapToTransactionResponse);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getExtractByType(Long accountId, TransactionType type, Pageable pageable) {
        User currentUser = getCurrentUser();
        Account account = getAccountAndValidateOwnership(accountId, currentUser);

        Page<Transaction> transactions = transactionRepository
                .findByAccountIdAndTransactionType(account.getId(), type, pageable);

        return transactions.map(this::mapToTransactionResponse);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getExtractByDateRange(Long accountId,
                                                             LocalDateTime startDate,
                                                             LocalDateTime endDate,
                                                             Pageable pageable) {
        User currentUser = getCurrentUser();
        Account account = getAccountAndValidateOwnership(accountId, currentUser);

        Page<Transaction> transactions = transactionRepository
                .findByAccountIdAndDateBetween(account.getId(), startDate, endDate, pageable);

        return transactions.map(this::mapToTransactionResponse);
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new BusinessException("Transação não encontrada"));

        // Validar propriedade
        User currentUser = getCurrentUser();
        if (!transaction.getAccount().getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("Transação não encontrada");
        }

        return mapToTransactionResponse(transaction);
    }

    private Account getAccountAndValidateOwnership(Long accountId, User user) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException("Conta não encontrada"));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Conta não pertence ao usuário");
        }

        return account;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));
    }

    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .transactionType(transaction.getTransactionType())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .transactionDate(transaction.getTransactionDate())
                .accountNumber(transaction.getAccount().getAccountNumber())
                .targetAccountNumber(transaction.getTargetAccount() != null ?
                        transaction.getTargetAccount().getAccountNumber() : null)
                .reversed(transaction.getReversed())
                .build();
    }
}

