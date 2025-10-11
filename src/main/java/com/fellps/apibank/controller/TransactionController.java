package com.fellps.apibank.controller;

import com.fellps.apibank.dto.*;
import com.fellps.apibank.enums.TransactionType;
import com.fellps.apibank.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/accounts/{accountId}/transactions")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Transações", description = "Endpoints para operações bancárias e consulta de extrato")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    @Operation(summary = "Realizar depósito", description = "Deposita um valor na conta")
    public ResponseEntity<TransactionResponse> deposit(
            @PathVariable Long accountId,
            @Valid @RequestBody DepositRequest request) {
        TransactionResponse response = transactionService.deposit(accountId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Realizar saque", description = "Saca um valor da conta")
    public ResponseEntity<TransactionResponse> withdraw(
            @PathVariable Long accountId,
            @Valid @RequestBody WithdrawRequest request) {
        TransactionResponse response = transactionService.withdraw(accountId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfer")
    @Operation(summary = "Realizar transferência", description = "Transfere um valor para outra conta")
    public ResponseEntity<TransactionResponse> transfer(
            @PathVariable Long accountId,
            @Valid @RequestBody TransferRequest request) {
        TransactionResponse response = transactionService.transfer(accountId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Consultar extrato", description = "Retorna o extrato da conta com paginação")
    public ResponseEntity<Page<TransactionResponse>> getExtract(
            @PathVariable Long accountId,
            @PageableDefault(size = 20, sort = "transactionDate") Pageable pageable) {
        Page<TransactionResponse> extract = transactionService.getExtract(accountId, pageable);
        return ResponseEntity.ok(extract);
    }

    @GetMapping("/filter")
    @Operation(summary = "Consultar extrato por tipo", description = "Retorna o extrato filtrado por tipo de transação")
    public ResponseEntity<Page<TransactionResponse>> getExtractByType(
            @PathVariable Long accountId,
            @RequestParam TransactionType type,
            @PageableDefault(size = 20, sort = "transactionDate") Pageable pageable) {
        Page<TransactionResponse> extract = transactionService.getExtractByType(accountId, type, pageable);
        return ResponseEntity.ok(extract);
    }

    @GetMapping("/date-range")
    @Operation(summary = "Consultar extrato por período", description = "Retorna o extrato em um período específico")
    public ResponseEntity<Page<TransactionResponse>> getExtractByDateRange(
            @PathVariable Long accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 20, sort = "transactionDate") Pageable pageable) {
        Page<TransactionResponse> extract = transactionService.getExtractByDateRange(accountId, startDate, endDate, pageable);
        return ResponseEntity.ok(extract);
    }

    @GetMapping("/{transactionId}/receipt")
    @Operation(summary = "Obter comprovante", description = "Retorna o comprovante de uma transação específica")
    public ResponseEntity<TransactionResponse> getReceipt(
            @PathVariable Long accountId,
            @PathVariable Long transactionId) {
        TransactionResponse transaction = transactionService.getTransactionById(transactionId);
        return ResponseEntity.ok(transaction);
    }
}

