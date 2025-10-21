package ru.vvsem.bank.analyzer.models;

import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for {@link BankAccount}
 */
@Value
public class BankAccountSimpleDto {
    Long id;
    @NotNull
    String name;
    @NotNull
    String accountNumber;
    @NotNull
    BigDecimal balance;
    Long bankId;
    @NotNull
    String bankName;
    @NotNull
    String currencyCode;
    @NotNull
    String currencySymbol;
    List<CardDto> cards;

    /**
     * DTO for {@link Card}
     */
    @Value
    public static class CardDto {
        Long id;
        String lastFourDigits;
        String cardName;
    }
}