package ru.vvsem.bank.analyzer.dto.analytics;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.vvsem.bank.analyzer.dto.category.CategoryDto;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class CategoryBreakdownDto {
    @NotNull
    private CategoryDto category;

    @NotNull
    private BigDecimal amount;
}
