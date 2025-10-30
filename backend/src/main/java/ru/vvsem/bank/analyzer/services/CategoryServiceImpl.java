package ru.vvsem.bank.analyzer.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vvsem.bank.analyzer.dto.CategoryDto;
import ru.vvsem.bank.analyzer.exceptions.EntityNotFoundException;
import ru.vvsem.bank.analyzer.mappers.CategoryMapper;
import ru.vvsem.bank.analyzer.models.Category;
import ru.vvsem.bank.analyzer.repositories.CategoryRepository;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    private final CategoryMapper categoryMapper;

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
}
