package ru.vvsem.bank.analyzer.dto.category;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * DTO for {@link ru.vvsem.bank.analyzer.models.Category}
 */
@Getter
@Setter
@NoArgsConstructor
public class CategoryDto {
    private  Long id;

    @NotNull
    private  String name;

    @NotNull
    private  String color;

    private String textColor;

}