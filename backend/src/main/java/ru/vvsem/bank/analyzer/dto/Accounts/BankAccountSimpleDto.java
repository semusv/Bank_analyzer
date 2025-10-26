package ru.vvsem.bank.analyzer.dto.Accounts;

import jakarta.validation.constraints.NotNull;
import lombok.Value;
import ru.vvsem.bank.analyzer.models.BankAccount;
import ru.vvsem.bank.analyzer.models.Card;

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