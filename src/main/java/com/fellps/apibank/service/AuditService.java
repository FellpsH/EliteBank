package com.fellps.apibank.service;

import com.fellps.apibank.model.Account;
import com.fellps.apibank.model.AuditLog;
import com.fellps.apibank.model.Transaction;
import com.fellps.apibank.model.User;
import com.fellps.apibank.repository.AuditLogRepository;
import com.fellps.apibank.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public void logUserCreation(User user) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .entityName("User")
                    .entityId(user.getId())
                    .action("CREATE")
                    .performedBy(user)
                    .newValue(objectMapper.writeValueAsString(user))
                    .description("Novo usuário cadastrado")
                    .build();

            auditLogRepository.save(auditLog);
        } catch (JsonProcessingException e) {
            log.error("Error creating audit log for user creation", e);
        }
    }

    @Transactional
    public void logTransaction(Transaction transaction, User performedBy) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .entityName("Transaction")
                    .entityId(transaction.getId())
                    .action(transaction.getTransactionType().name())
                    .performedBy(performedBy)
                    .newValue(objectMapper.writeValueAsString(transaction))
                    .description(String.format("Transação: %s - R$ %.2f",
                            transaction.getTransactionType().getDescription(),
                            transaction.getAmount()))
                    .build();

            auditLogRepository.save(auditLog);
        } catch (JsonProcessingException e) {
            log.error("Error creating audit log for transaction", e);
        }
    }

    @Transactional
    public void logAccountUpdate(Account account, String action, String description) {
        try {
            User currentUser = getCurrentUser();

            AuditLog auditLog = AuditLog.builder()
                    .entityName("Account")
                    .entityId(account.getId())
                    .action(action)
                    .performedBy(currentUser)
                    .newValue(objectMapper.writeValueAsString(account))
                    .description(description)
                    .build();

            auditLogRepository.save(auditLog);
        } catch (JsonProcessingException e) {
            log.error("Error creating audit log for account update", e);
        }
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByEntity(String entityName, Long entityId, Pageable pageable) {
        return auditLogRepository.findByEntityNameAndEntityId(entityName, entityId, pageable);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            return userRepository.findByEmail(email).orElse(null);
        }
        return null;
    }
}

