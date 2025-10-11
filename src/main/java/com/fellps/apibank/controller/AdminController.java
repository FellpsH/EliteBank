package com.fellps.apibank.controller;

import com.fellps.apibank.dto.TransactionResponse;
import com.fellps.apibank.dto.UserResponse;
import com.fellps.apibank.model.AuditLog;
import com.fellps.apibank.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administração", description = "Endpoints administrativos (apenas para administradores)")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    @Operation(summary = "Listar todos os usuários", description = "Retorna todos os usuários do sistema (paginado)")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<UserResponse> users = adminService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/transactions")
    @Operation(summary = "Listar todas as transações", description = "Retorna todas as transações do sistema (paginado)")
    public ResponseEntity<Page<TransactionResponse>> getAllTransactions(
            @PageableDefault(size = 20, sort = "transactionDate") Pageable pageable) {
        Page<TransactionResponse> transactions = adminService.getAllTransactions(pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Consultar logs de auditoria", description = "Retorna todos os logs de auditoria do sistema")
    public ResponseEntity<Page<AuditLog>> getAuditLogs(
            @PageableDefault(size = 20, sort = "performedAt") Pageable pageable) {
        Page<AuditLog> auditLogs = adminService.getAuditLogs(pageable);
        return ResponseEntity.ok(auditLogs);
    }
}

