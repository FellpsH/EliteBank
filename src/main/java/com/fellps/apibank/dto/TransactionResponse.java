package com.fellps.apibank.dto;

import com.fellps.apibank.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private TransactionType transactionType;
    private BigDecimal amount;
    private String description;
    private LocalDateTime transactionDate;
    private String accountNumber;
    private String targetAccountNumber;
    private Boolean reversed;
}

