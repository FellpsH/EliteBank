package com.fellps.apibank.dto;

import com.fellps.apibank.enums.AccountType;
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
public class AccountResponse {
    private Long id;
    private String accountNumber;
    private String agency;
    private AccountType accountType;
    private BigDecimal balance;
    private Boolean active;
    private LocalDateTime createdAt;
}

