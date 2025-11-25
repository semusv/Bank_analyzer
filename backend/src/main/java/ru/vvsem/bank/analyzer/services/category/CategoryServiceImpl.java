package ru.vvsem.bank.analyzer.services.category;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategoriesForUser(SecurityUser securityUser) {

        return categoryRepository.findByUserId(securityUser.getId())
                .stream()
                .map(categoryMapper::toCategoryDto)
                .sorted(Comparator.comparing(CategoryDto::getName))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getCategoryEntitiesForUser(SecurityUser securityUser) {
        return categoryRepository.findByUserId(securityUser.getId())
                .stream()
                .sorted(Comparator.comparing(Category::getName))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getCategoryDtoById(Long categoryId, SecurityUser securityUser) {
        return categoryMapper.toCategoryDto(entityAccessProviderImpl.getOwnedCategory(
                categoryId, securityUser.getId()));
    }

    @Override
    @Transactional
    public CategoryDto createCategory(CategoryDto categoryDto, SecurityUser securityUser) {

        Category category = categoryMapper.toEntity(categoryDto);
        category.setUser(userService.getUserById(securityUser.getId()));

        return categoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    @Override
    public CategoryColorsDto getCategoryColors() {
        return new CategoryColorsDto(
                categoryColorConfig.getBackgroundColorsList(),
                categoryColorConfig.getTextColorsList());
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long categoryId, CategoryDto categoryDto, SecurityUser securityUser) {
        Category category = entityAccessProviderImpl.getOwnedCategory(categoryId, securityUser.getId());
        category.setName(categoryDto.getName());
        category.setColor(categoryDto.getColor());
        category.setTextColor(categoryDto.getTextColor());
        category.setUser(userService.getUserById(securityUser.getId()));
        return categoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId, SecurityUser securityUser) {
        Category category = entityAccessProviderImpl.getOwnedCategory(categoryId, securityUser.getId());
        categoryRepository.delete(category);
    }

}
