package ru.vvsem.bank.analyzer.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

/**
 * DTO for {@link ru.vvsem.bank.analyzer.models.Budget}
 */
@Getter
@Setter
public class BudgetDto {
    private Long id;

    @NotNull
    private BigDecimal limitAmount;

    @NotNull
    private  CategoryDto category;

    private Long userId;
}