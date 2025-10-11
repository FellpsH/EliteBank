package com.fellps.apibank.service;

import com.fellps.apibank.dto.AccountResponse;
import com.fellps.apibank.exception.ResourceNotFoundException;
import com.fellps.apibank.model.Account;
import com.fellps.apibank.model.User;
import com.fellps.apibank.repository.AccountRepository;
import com.fellps.apibank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AccountResponse> getMyAccounts() {
        User currentUser = getCurrentUser();
        List<Account> accounts = accountRepository.findByUserId(currentUser.getId());

        return accounts.stream()
                .map(this::mapToAccountResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long accountId) {
        User currentUser = getCurrentUser();
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada"));

        // Verificar se a conta pertence ao usuário
        if (!account.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Conta não encontrada");
        }

        return mapToAccountResponse(account);
    }

    @Transactional(readOnly = true)
    public Account getAccountEntityById(Long accountId) {
        User currentUser = getCurrentUser();
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada"));

        // Verificar se a conta pertence ao usuário
        if (!account.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Conta não encontrada");
        }

        return account;
    }

    @Transactional(readOnly = true)
    public Account getAccountByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada"));
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    private AccountResponse mapToAccountResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .agency(account.getAgency())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .active(account.getActive())
                .createdAt(account.getCreatedAt())
                .build();
    }
}

