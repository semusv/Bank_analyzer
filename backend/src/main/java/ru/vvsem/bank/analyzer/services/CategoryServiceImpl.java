package ru.vvsem.bank.analyzer.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vvsem.bank.analyzer.configs.CategoryColorConfig;
import ru.vvsem.bank.analyzer.dto.category.CategoryColorsDto;
import ru.vvsem.bank.analyzer.dto.category.CategoryDto;
import ru.vvsem.bank.analyzer.mappers.CategoryMapper;
import ru.vvsem.bank.analyzer.models.Category;
import ru.vvsem.bank.analyzer.models.User;
import ru.vvsem.bank.analyzer.providers.EntityAccessProviderImpl;
import ru.vvsem.bank.analyzer.repositories.CategoryRepository;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    private final CategoryMapper categoryMapper;

    private final CategoryColorConfig categoryColorConfig;

    private final EntityAccessProviderImpl entityAccessProviderImpl;

    @Override
    public List<CategoryDto> getCategoriesForUser(User user) {

        return categoryRepository.findByUserId(user.getId())
                .stream()
                .map(categoryMapper::toCategoryDto)
                .sorted( (categoryDto1, categoryDto2) -> categoryDto1.getName().compareTo(categoryDto2.getName()))
                .toList();
    }

    @Override
    public CategoryDto getCategoryDtoById(Long categoryId, User user) {
        return categoryMapper.toCategoryDto(entityAccessProviderImpl.requireOwnedCategory(categoryId, user.getId()));
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

    @Override
    public CategoryDto updateCategory(Long categoryId, CategoryDto categoryDto, User user) {
        Category category = entityAccessProviderImpl.requireOwnedCategory(categoryId, user.getId());
        category.setName(categoryDto.getName());
        category.setColor(categoryDto.getColor());
        category.setTextColor(categoryDto.getTextColor());
        category.setUser(user);
        return categoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    public void deleteCategory(Long categoryId, User user) {
        Category category = entityAccessProviderImpl.requireOwnedCategory(categoryId, user.getId());
        categoryRepository.delete(category);
    }

}
