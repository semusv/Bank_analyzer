package ru.vvsem.bank.analyzer.dto.account;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.vvsem.bank.analyzer.models.BankAccount;
import ru.vvsem.bank.analyzer.models.Card;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for {@link BankAccount}
 */
@Getter
@Setter
public class BankAccountSimpleDto {
    private Long id;

    @NotNull
    private String name;

    @NotNull
    private String accountNumber;

    @NotNull
    private BigDecimal balance;

    @NotNull
    private Long bankId;

    @NotNull
    private String bankCode;

    @NotNull
    private String bankName;

    @NotNull
    private String currencyCode;

    @NotNull
    private String currencySymbol;

    private List<CardDto> cards;

    /**
     * DTO for {@link Card}
     */
    @Setter
    @Getter
    public static class CardDto {
        private Long id;

        private String lastFourDigits;

        private String cardName;
    }
}