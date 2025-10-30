package ru.vvsem.bank.analyzer.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for {@link ru.vvsem.bank.analyzer.models.BankTheme}
 */
@Getter
@Setter
public class BankThemeDto {
    @NotNull
    private String primaryColor;

    @NotNull
    private String textColor;

    @NotNull
    private String secondaryColor;
}
