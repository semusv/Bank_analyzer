package ru.vvsem.bank.analyzer.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Value;

/**
 * DTO for {@link ru.vvsem.bank.analyzer.models.Bank}
 */
@Value
public class BankDto {

    Long id;

    @NotNull
    String name;

    @NotNull
    String bic;

    String logoUrl;
}