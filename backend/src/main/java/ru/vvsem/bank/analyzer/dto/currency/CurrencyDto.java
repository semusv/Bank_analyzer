package ru.vvsem.bank.analyzer.dto.currency;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


/**
 * DTO for {@link ru.vvsem.bank.analyzer.models.Currency}
 */
@Getter
@Setter
public class CurrencyDto {
    private  Long id;

    @NotNull
    private   String code;

    @NotNull
    private  String symbol;

    @NotNull
    private  String name;
}