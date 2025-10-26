package ru.vvsem.bank.analyzer.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.vvsem.bank.analyzer.dto.Accounts.BankAccountDto;
import ru.vvsem.bank.analyzer.models.Card;

/**
 * DTO for {@link Card}
 */
@Getter
@Setter
public class CardDto {
    private Long id;

    @NotNull
    private String lastFourDigits;

    @NotNull
    private String cardName;

    @NotNull
    private BankAccountDto account;

    @NotNull
    private BankDto issuerBank;
}