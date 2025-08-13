package ru.vvsem.bank.analyzer.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


/**
 * DTO for {@link ru.vvsem.bank.analyzer.models.Category}
 */
@Getter
@Setter
public class CategoryDto {
    private  Long id;

    @NotNull
    private  String name;

    private  String color;
}