package ru.vvsem.bank.analyzer.services.category;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vvsem.bank.analyzer.configs.theme.CategoryColorConfig;
import ru.vvsem.bank.analyzer.dto.category.CategoryColorsDto;
import ru.vvsem.bank.analyzer.dto.category.CategoryDto;
import ru.vvsem.bank.analyzer.mappers.CategoryMapper;
import ru.vvsem.bank.analyzer.models.Category;
import ru.vvsem.bank.analyzer.models.SecurityUser;
import ru.vvsem.bank.analyzer.providers.EntityAccessProviderImpl;
import ru.vvsem.bank.analyzer.repositories.CategoryRepository;
import ru.vvsem.bank.analyzer.services.security.CustomUserDetailsService;

import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    private final CategoryMapper categoryMapper;

    private final CategoryColorConfig categoryColorConfig;

    private final EntityAccessProviderImpl entityAccessProviderImpl;

    private final CustomUserDetailsService userService;

    @Override
    public List<CategoryDto> getCategoriesForUser(SecurityUser securityUser) {

        return categoryRepository.findByUserId(securityUser.getId())
                .stream()
                .map(categoryMapper::toCategoryDto)
                .sorted(Comparator.comparing(CategoryDto::getName))
                .toList();
    }

    @Override
    public List<Category> getCategoryEntitiesForUser(SecurityUser securityUser) {
        return categoryRepository.findByUserId(securityUser.getId())
                .stream()
                .sorted(Comparator.comparing(Category::getName))
                .toList();
    }

    @Override
    public CategoryDto getCategoryDtoById(Long categoryId, SecurityUser securityUser) {
        return categoryMapper.toCategoryDto(entityAccessProviderImpl.requireOwnedCategory(
                categoryId, securityUser.getId()));
    }

    @Override
    public CategoryDto createCategory(CategoryDto categoryDto, SecurityUser securityUser) {

        Category category = categoryMapper.toEntity(categoryDto);
        category.setUser(userService.getUserById(securityUser.getId()));

        return categoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    public CategoryColorsDto getCategoryColors() {
        return new CategoryColorsDto(
                categoryColorConfig.getBackgroundColorsList(),
                categoryColorConfig.getTextColorsList());
    }

    @Override
    public CategoryDto updateCategory(Long categoryId, CategoryDto categoryDto, SecurityUser securityUser) {
        Category category = entityAccessProviderImpl.requireOwnedCategory(categoryId, securityUser.getId());
        category.setName(categoryDto.getName());
        category.setColor(categoryDto.getColor());
        category.setTextColor(categoryDto.getTextColor());
        category.setUser(userService.getUserById(securityUser.getId()));
        return categoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    public void deleteCategory(Long categoryId, SecurityUser securityUser) {
        Category category = entityAccessProviderImpl.requireOwnedCategory(categoryId, securityUser.getId());
        categoryRepository.delete(category);
    }

}
