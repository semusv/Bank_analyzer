package ru.vvsem.bank.analyzer.dto;

import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import ru.vvsem.bank.analyzer.models.BankAccount;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for {@link BankAccount}
 */
@Getter
@Setter
public class BankAccountDto {

    private Long id;

    @NotNull
    private String name;

    @NotNull
    private String accountNumber;

    @NotNull
    private BankDto bank;

    private Long currencyId;

    @NotNull
    private String currencyCode;

    @NotNull
    private CurrencyDto currency;

    private Long userId;

    private  List<CardDto> cards;

    @NotNull
    private BigDecimal balance;
}