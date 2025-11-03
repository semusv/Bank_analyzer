package ru.vvsem.bank.analyzer.controllers.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.vvsem.bank.analyzer.dto.category.CategoryColorsDto;
import ru.vvsem.bank.analyzer.dto.category.CategoryDto;
import ru.vvsem.bank.analyzer.models.User;
import ru.vvsem.bank.analyzer.services.category.CategoryService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/category")
@Slf4j
public class CategoryApiController {

    private final CategoryService categoryService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CategoryDto> getAllCategories(
            @AuthenticationPrincipal User user
    ) {
        return categoryService.getCategoriesForUser(user);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto getCategoryById(
            @PathVariable("id") Long categoryId,
            @AuthenticationPrincipal User user
    ) {
        return categoryService.getCategoryDtoById(categoryId, user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(
            @Valid @RequestBody CategoryDto categoryDto,
            @AuthenticationPrincipal User user
    ) {
        return categoryService.createCategory(categoryDto, user);
    }

    @GetMapping("/colors")
    @ResponseStatus(HttpStatus.OK)
    public CategoryColorsDto getCategoryColors() {
        return categoryService.getCategoryColors();
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto updateCategory(
            @PathVariable("id") Long categoryId,
            @Valid @RequestBody CategoryDto categoryDto,
            @AuthenticationPrincipal User user
    ) {
        return categoryService.updateCategory(categoryId, categoryDto, user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(
            @PathVariable("id") Long categoryId,
            @AuthenticationPrincipal User user)
    {
        categoryService.deleteCategory(categoryId, user);
    }
}
