package ru.vvsem.bank.analyzer.services;

import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.category.CategoryColorsDto;
import ru.vvsem.bank.analyzer.dto.category.CategoryDto;
import ru.vvsem.bank.analyzer.models.Category;
import ru.vvsem.bank.analyzer.models.User;

import java.util.List;

public interface CategoryService {
    @Transactional
    List<CategoryDto> getCategoriesForUser(Long userId);

    Category getCategoryById(Long categoryId);

    CategoryDto getCategoryDtoById(Long categoryId);

    CategoryDto createCategory(@Valid CategoryDto categoryDto, User user);

    CategoryColorsDto getCategoryColors();
}
