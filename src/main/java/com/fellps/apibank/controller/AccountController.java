package com.fellps.apibank.controller;

import com.fellps.apibank.dto.AccountResponse;
import com.fellps.apibank.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Contas", description = "Endpoints para gerenciamento de contas bancárias")
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    @Operation(summary = "Listar minhas contas", description = "Retorna todas as contas do usuário autenticado")
    public ResponseEntity<List<AccountResponse>> getMyAccounts() {
        List<AccountResponse> accounts = accountService.getMyAccounts();
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Consultar conta", description = "Retorna os dados de uma conta específica incluindo saldo")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable Long accountId) {
        AccountResponse account = accountService.getAccountById(accountId);
        return ResponseEntity.ok(account);
    }
}

