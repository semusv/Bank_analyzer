package ru.vvsem.bank.analyzer.services.category;

import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.category.CategoryColorsDto;
import ru.vvsem.bank.analyzer.dto.category.CategoryDto;
import ru.vvsem.bank.analyzer.models.Category;
import ru.vvsem.bank.analyzer.models.SecurityUser;

import java.util.List;

public interface CategoryService {
    @Transactional(readOnly = true)
    List<CategoryDto> getCategoriesForUser(SecurityUser securityUser);

    @Transactional(readOnly = true)
    List<Category> getCategoryEntitiesForUser(SecurityUser securityUser);

    @Transactional(readOnly = true)
    CategoryDto getCategoryDtoById(Long categoryId, SecurityUser securityUser);

    @Transactional
    CategoryDto createCategory(@Valid CategoryDto categoryDto, SecurityUser securityUser);

    @Transactional(readOnly = true)
    CategoryColorsDto getCategoryColors();

    @Transactional
    CategoryDto updateCategory(Long categoryId, @Valid CategoryDto categoryDto, SecurityUser securityUser);

    @Transactional
    void deleteCategory(Long categoryId, SecurityUser securityUser);
}
