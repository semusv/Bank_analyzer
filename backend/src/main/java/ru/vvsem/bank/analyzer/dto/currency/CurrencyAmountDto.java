package ru.vvsem.bank.analyzer.dto.currency;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class CurrencyAmountDto {
    @NotBlank
    @Size(min = 3,max = 3)
    private String currencyCode;

    private BigDecimal amount;
}