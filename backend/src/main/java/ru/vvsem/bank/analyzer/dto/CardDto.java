package ru.vvsem.bank.analyzer.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Value;
import ru.vvsem.bank.analyzer.models.Card;

/**
 * DTO for {@link Card}
 */
@Value
public class CardDto {
    Long id;

    @NotNull
    String lastFourDigits;

    @NotNull
    String cardName;

    @NotNull
    BankAccountDto account;

    @NotNull
    BankDto issuerBank;
}