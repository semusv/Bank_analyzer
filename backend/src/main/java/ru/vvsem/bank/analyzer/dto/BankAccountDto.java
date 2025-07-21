package ru.vvsem.bank.analyzer.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Value;
import ru.vvsem.bank.analyzer.models.BankAccount;

import java.util.List;

/**
 * DTO for {@link BankAccount}
 */
@Value
public class BankAccountDto {
    Long id;

    @NotNull
    String name;

    @NotNull
    String accountNumber;

    @NotNull
    BankDto bank;

    Long currencyId;

    @NotNull
    String currencyCode;

    @NotNull
    CurrencyDto currency;

    Long userId;

    List<CardDto> cards;
}