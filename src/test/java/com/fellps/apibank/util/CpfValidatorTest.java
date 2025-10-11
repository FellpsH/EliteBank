package com.fellps.apibank.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class CpfValidatorTest {

    @ParameterizedTest
    @DisplayName("Deve validar CPFs válidos")
    @ValueSource(strings = {
            "52998224725",
            "11144477735",
            "12345678909"
    })
    void shouldValidateValidCpfs(String cpf) {
        assertTrue(CpfValidator.isValid(cpf));
    }

    @ParameterizedTest
    @DisplayName("Deve invalidar CPFs inválidos")
    @ValueSource(strings = {
            "12345678901",
            "00000000000",
            "11111111111",
            "99999999999"
    })
    void shouldInvalidateInvalidCpfs(String cpf) {
        assertFalse(CpfValidator.isValid(cpf));
    }

    @Test
    @DisplayName("Deve invalidar CPF null")
    void shouldInvalidateNullCpf() {
        assertFalse(CpfValidator.isValid(null));
    }

    @Test
    @DisplayName("Deve invalidar CPF com menos de 11 dígitos")
    void shouldInvalidateCpfWithLessThan11Digits() {
        assertFalse(CpfValidator.isValid("123456789"));
    }

    @Test
    @DisplayName("Deve invalidar CPF com mais de 11 dígitos")
    void shouldInvalidateCpfWithMoreThan11Digits() {
        assertFalse(CpfValidator.isValid("123456789012"));
    }

    @Test
    @DisplayName("Deve invalidar CPF com caracteres não numéricos")
    void shouldInvalidateCpfWithNonNumericCharacters() {
        assertFalse(CpfValidator.isValid("123.456.789-01"));
    }
}

