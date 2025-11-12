package ru.vvsem.bank.analyzer.dto.currency;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


/**
 * DTO for {@link ru.vvsem.bank.analyzer.models.Currency}
 */
@Getter
@Setter
public class CurrencyDto {
    private  Long id;

    @NotBlank
    @Size(min = 3,max = 3)
    private   String code;

    @NotBlank
    @Size(min = 1,max = 1)
    private  String symbol;

    @NotBlank
    private  String name;
}