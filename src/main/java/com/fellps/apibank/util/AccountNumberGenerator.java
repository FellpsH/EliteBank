package com.fellps.apibank.util;

import java.security.SecureRandom;

public class AccountNumberGenerator {
    
    private static final SecureRandom random = new SecureRandom();
    private static final String AGENCY = "0001";

    public static String generateAccountNumber() {
        // Gera um número de conta com 8 dígitos + 1 dígito verificador
        int accountNumber = 10000000 + random.nextInt(90000000);
        int checkDigit = calculateCheckDigit(accountNumber);
        return String.format("%08d-%d", accountNumber, checkDigit);
    }

    public static String getDefaultAgency() {
        return AGENCY;
    }

    private static int calculateCheckDigit(int accountNumber) {
        String number = String.valueOf(accountNumber);
        int sum = 0;
        int weight = 2;

        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(number.charAt(i));
            sum += digit * weight;
            weight = (weight == 9) ? 2 : weight + 1;
        }

        int remainder = sum % 11;
        return (remainder < 2) ? 0 : 11 - remainder;
    }
}

