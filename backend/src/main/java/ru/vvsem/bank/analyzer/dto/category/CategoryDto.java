package ru.vvsem.bank.analyzer.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    private Long id;

    @NotBlank(message = "{validation.сategory.name.notBlank}")
    private String name;


    @NotBlank(message = "{validation.сategory.color.notBlank}")
    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "{validation.сategory.color.pattern}")
    private String color;


    @NotBlank(message = "{validation.сategory.textColor.notBlank}")
    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "{validation.сategory.color.pattern}")
    private String textColor;

}