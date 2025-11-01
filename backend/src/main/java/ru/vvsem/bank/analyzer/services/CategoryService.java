package ru.vvsem.bank.analyzer.services;

import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.category.CategoryColorsDto;
import ru.vvsem.bank.analyzer.dto.category.CategoryDto;
import ru.vvsem.bank.analyzer.models.User;

import java.util.List;

public interface CategoryService {
    @Transactional
    List<CategoryDto> getCategoriesForUser(User user);

    CategoryDto getCategoryDtoById(Long categoryId, User user);

    CategoryDto createCategory(@Valid CategoryDto categoryDto, User user);

    CategoryColorsDto getCategoryColors();

    CategoryDto updateCategory(Long categoryId, @Valid CategoryDto categoryDto, User user);
}
