package ru.vvsem.bank.analyzer.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Value;

/**
 * DTO for {@link ru.vvsem.bank.analyzer.models.Category}
 */
@Value
public class CategoryDto {
    Long id;

    @NotNull
    String name;

    String color;
}