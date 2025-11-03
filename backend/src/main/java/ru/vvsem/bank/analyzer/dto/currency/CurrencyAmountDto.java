package ru.vvsem.bank.analyzer.dto.currency;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class CurrencyAmountDto {
    private String currencyCode;

    private BigDecimal amount;
}