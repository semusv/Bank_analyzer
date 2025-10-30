package ru.vvsem.bank.analyzer.services;

import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.CategoryDto;
import ru.vvsem.bank.analyzer.models.Category;

import java.util.List;

public interface CategoryService {
    @Transactional
    List<CategoryDto> getCategoriesForUser(Long userId);

    Category getCategoryById(Long categoryId);
}
