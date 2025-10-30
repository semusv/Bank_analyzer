package ru.vvsem.bank.analyzer.dto.bank;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for {@link ru.vvsem.bank.analyzer.models.Bank}
 */
@Getter
@Setter
public class BankDto {
    private Long id;

    @NotNull
    private  String name;

    @NotNull
    private  String bic;

    private String logoUrl;

    private String bankCode;
}