package ru.vvsem.bank.analyzer.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Value;

/**
 * DTO for {@link ru.vvsem.bank.analyzer.models.Currency}
 */
@Value
public class CurrencyDto {
    Long id;
    @NotNull
    String code;
    @NotNull
    String symbol;
    @NotNull
    String name;
}