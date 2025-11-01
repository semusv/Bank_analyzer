package ru.vvsem.bank.analyzer.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vvsem.bank.analyzer.configs.CategoryColorConfig;
import ru.vvsem.bank.analyzer.dto.category.CategoryColorsDto;
import ru.vvsem.bank.analyzer.dto.category.CategoryDto;
import ru.vvsem.bank.analyzer.exceptions.EntityNotFoundException;
import ru.vvsem.bank.analyzer.mappers.CategoryMapper;
import ru.vvsem.bank.analyzer.models.Category;
import ru.vvsem.bank.analyzer.models.User;
import ru.vvsem.bank.analyzer.repositories.CategoryRepository;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    private final CategoryMapper categoryMapper;

    private final CategoryColorConfig categoryColorConfig;

    @Override
    public List<CategoryDto> getCategoriesForUser(Long userId) {

        return categoryRepository.findByUserId(userId)
                .stream()
                .map(categoryMapper::toCategoryDto)
                .toList();
    }

    @Override
    public Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Currency for card with id %d not found".formatted(categoryId),
                        "exception.entity.not.found.category"));
    }

    @Override
    public CategoryDto getCategoryDtoById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .map(categoryMapper::toCategoryDto)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Currency for card with id %d not found".formatted(categoryId),
                        "exception.entity.not.found.category"));
    }

    @Override
    public CategoryDto createCategory(CategoryDto categoryDto, User user) {

        Category category = categoryMapper.toEntity(categoryDto);
        category.setUser(user);

        return categoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    public CategoryColorsDto getCategoryColors() {
        return new CategoryColorsDto(
                categoryColorConfig.getBackgroundColorsList(),
                categoryColorConfig.getTextColorsList());
    }
}
