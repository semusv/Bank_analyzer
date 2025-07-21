package ru.vvsem.bank.analyzer.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.math.BigDecimal;

/**
 * DTO for {@link ru.vvsem.bank.analyzer.models.Budget}
 */
@Value
public class BudgetDto {
    Long id;
    @NotNull
    BigDecimal limitAmount;
    @NotNull
    CategoryDto category;
    Long userId;
}