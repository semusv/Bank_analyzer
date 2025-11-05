package ru.vvsem.bank.analyzer.services.category;

import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.category.CategoryColorsDto;
import ru.vvsem.bank.analyzer.dto.category.CategoryDto;
import ru.vvsem.bank.analyzer.models.SecurityUser;

import java.util.List;

public interface CategoryService {
    @Transactional
    List<CategoryDto> getCategoriesForUser(SecurityUser securityUser);

    CategoryDto getCategoryDtoById(Long categoryId, SecurityUser securityUser);

    CategoryDto createCategory(@Valid CategoryDto categoryDto, SecurityUser securityUser);

    CategoryColorsDto getCategoryColors();

    CategoryDto updateCategory(Long categoryId, @Valid CategoryDto categoryDto, SecurityUser securityUser);

    void deleteCategory(Long categoryId, SecurityUser securityUser);
}
