package com.fellps.apibank.enums;

public enum TransactionType {
    DEPOSITO("Depósito"),
    SAQUE("Saque"),
    TRANSFERENCIA_ENVIADA("Transferência Enviada"),
    TRANSFERENCIA_RECEBIDA("Transferência Recebida");

    private final String description;

    TransactionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

