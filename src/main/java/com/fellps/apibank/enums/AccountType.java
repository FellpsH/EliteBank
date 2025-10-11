package com.fellps.apibank.enums;

public enum AccountType {
    CORRENTE("Conta Corrente"),
    POUPANCA("Conta Poupança");

    private final String description;

    AccountType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

