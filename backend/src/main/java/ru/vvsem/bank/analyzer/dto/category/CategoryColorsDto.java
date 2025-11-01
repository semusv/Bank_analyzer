package ru.vvsem.bank.analyzer.dto.category;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class CategoryColorsDto {

    private List<String> backgroundColors;

    private List<String> textColors;
}
