package ru.vvsem.bank.analyzer.dto.card;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.vvsem.bank.analyzer.dto.CurrencyDto;
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
    private Long accountId;

    @NotNull
    private Long issuerBankId;

    @NotNull
    private CurrencyDto currency;

    @NotNull
    private String bankCode;
}