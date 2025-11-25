package ru.vvsem.bank.analyzer.services.category;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.vvsem.bank.analyzer.configs.theme.CategoryColorConfig;
import ru.vvsem.bank.analyzer.dto.category.CategoryColorsDto;
import ru.vvsem.bank.analyzer.dto.category.CategoryDto;
import ru.vvsem.bank.analyzer.mappers.CategoryMapperImpl;
import ru.vvsem.bank.analyzer.models.Category;
import ru.vvsem.bank.analyzer.models.SecurityUser;
import ru.vvsem.bank.analyzer.models.User;
import ru.vvsem.bank.analyzer.providers.EntityAccessProviderImpl;
import ru.vvsem.bank.analyzer.repositories.BaseRepositoryTest;
import ru.vvsem.bank.analyzer.repositories.UserRepository;
import ru.vvsem.bank.analyzer.services.security.CustomUserDetailsService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DataJpaTest
@Testcontainers
@TestPropertySource("classpath:application.yml")
@Import({
        CategoryServiceImpl.class,
        CategoryMapperImpl.class,
        EntityAccessProviderImpl.class
})
class CategoryServiceIntegrationTest extends BaseRepositoryTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private CategoryColorConfig categoryColorConfig;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private SecurityUser securityUser;
    private User user;
    private User anotherUser;
    private Category category1;
    private Category category2;
    private Category anotherUserCategory;

    @BeforeEach
    void setUp() {
        // Создаём пользователей
        user = new User();
        user.setLogin("testuser");
        user.setEmail("test@example.com");
        user.setPassword("pass");
        user.setName("Test");
        user.setSurname("User");
        user = userRepository.saveAndFlush(user);

        anotherUser = new User();
        anotherUser.setLogin("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("pass");
        anotherUser.setName("Another");
        anotherUser.setSurname("User");
        anotherUser = userRepository.saveAndFlush(anotherUser);

        securityUser = new SecurityUser(
                user.getId(), user.getLogin(), user.getPassword(), null,
                true, true, true, true
        );

        when(customUserDetailsService.getUserById(user.getId())).thenReturn(user);

        // Создаём категории для основного пользователя
        category1 = new Category();
        category1.setName("Food");
        category1.setColor("#FF0000");
        category1.setTextColor("#FFFFFF");
        category1.setUser(user);

        category2 = new Category();
        category2.setName("Salary");
        category2.setColor("#00FF00");
        category2.setTextColor("#000000");
        category2.setUser(user);

        category1 = entityManager.persistAndFlush(category1);
        category2 = entityManager.persistAndFlush(category2);

        // Создаём категорию для другого пользователя
        anotherUserCategory = new Category();
        anotherUserCategory.setName("Entertainment");
        anotherUserCategory.setColor("#0000FF");
        anotherUserCategory.setTextColor("#FFFFFF");
        anotherUserCategory.setUser(anotherUser);
        entityManager.persistAndFlush(anotherUserCategory);

        entityManager.flush();
        entityManager.clear();


        // Мокаем конфиг цветов
        when(categoryColorConfig.getBackgroundColorsList()).thenReturn(List.of("#FF0000", "#00FF00", "#0000FF"));
        when(categoryColorConfig.getTextColorsList()).thenReturn(List.of("#FFFFFF", "#000000"));
    }

    @Test
    @DisplayName("Должен вернуть категории только для текущего пользователя")
    void shouldReturnCategoriesOnlyForCurrentUser() {
        // When
        List<CategoryDto> result = categoryService.getCategoriesForUser(securityUser);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(CategoryDto::getName)
                .containsExactlyInAnyOrder("Food", "Salary");
        assertThat(result)
                .extracting(CategoryDto::getId)
                .containsExactlyInAnyOrder(category1.getId(), category2.getId());
    }

    @Test
    @DisplayName("Должен вернуть отсортированные по имени категории")
    void shouldReturnCategoriesSortedByName() {
        // When
        List<CategoryDto> result = categoryService.getCategoriesForUser(securityUser);

        // Then
        assertThat(result)
                .extracting(CategoryDto::getName)
                .containsExactly("Food", "Salary"); // В алфавитном порядке
    }

    @Test
    @DisplayName("Должен вернуть пустой список когда категорий нет")
    void shouldReturnEmptyListWhenNoCategories() {
        // Given
        User newUser = new User();
        newUser.setLogin("newuser");
        newUser.setEmail("new@example.com");
        newUser.setPassword("pass");
        newUser.setName("New");
        newUser.setSurname("User");
        newUser = userRepository.saveAndFlush(newUser);

        SecurityUser newSecurityUser = new SecurityUser(
                newUser.getId(), newUser.getLogin(), newUser.getPassword(), null,
                true, true, true, true
        );

        when(customUserDetailsService.getUserById(newUser.getId())).thenReturn(newUser);

        // When
        List<CategoryDto> result = categoryService.getCategoriesForUser(newSecurityUser);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Должен вернуть сущности категорий для пользователя")
    void shouldReturnCategoryEntitiesForUser() {
        // When
        List<Category> result = categoryService.getCategoryEntitiesForUser(securityUser);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Category::getName)
                .containsExactly("Food", "Salary"); // Отсортированы по имени
        assertThat(result)
                .allMatch(category -> category.getUser().getId().equals(user.getId()));
    }

    @Test
    @DisplayName("Должен создать новую категорию")
    void shouldCreateNewCategory() {
        // Given
        CategoryDto newCategoryDto = new CategoryDto();
        newCategoryDto.setName("Transport");
        newCategoryDto.setColor("#FFFF00");
        newCategoryDto.setTextColor("#000000");

        // When
        CategoryDto result = categoryService.createCategory(newCategoryDto, securityUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Transport");
        assertThat(result.getColor()).isEqualTo("#FFFF00");
        assertThat(result.getTextColor()).isEqualTo("#000000");

        // Проверяем, что категория сохранилась в БД
        Category savedCategory = entityManager.find(Category.class, result.getId());
        assertThat(savedCategory).isNotNull();
        assertThat(savedCategory.getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("Должен обновить существующую категорию")
    void shouldUpdateExistingCategory() {
        // Given
        CategoryDto updateDto = new CategoryDto();
        updateDto.setName("Groceries");
        updateDto.setColor("#00FFFF");
        updateDto.setTextColor("#000000");

        // When
        CategoryDto result = categoryService.updateCategory(category1.getId(), updateDto, securityUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(category1.getId());
        assertThat(result.getName()).isEqualTo("Groceries");
        assertThat(result.getColor()).isEqualTo("#00FFFF");
        assertThat(result.getTextColor()).isEqualTo("#000000");

        // Проверяем, что категория обновилась в БД
        Category updatedCategory = entityManager.find(Category.class, category1.getId());
        assertThat(updatedCategory.getName()).isEqualTo("Groceries");
        assertThat(updatedCategory.getColor()).isEqualTo("#00FFFF");
    }

    @Test
    @DisplayName("Должен удалить категорию")
    void shouldDeleteCategory() {
        // Given
        Long categoryId = category1.getId();

        // When
        categoryService.deleteCategory(categoryId, securityUser);

        // Then
        Category deletedCategory = entityManager.find(Category.class, categoryId);
        assertThat(deletedCategory).isNull();
    }

    @Test
    @DisplayName("Должен вернуть категорию по ID")
    void shouldReturnCategoryById() {
        // When
        CategoryDto result = categoryService.getCategoryDtoById(category2.getId(), securityUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(category2.getId());
        assertThat(result.getName()).isEqualTo("Salary");
        assertThat(result.getColor()).isEqualTo("#00FF00");
    }

    @Test
    @DisplayName("Должен вернуть доступные цвета категорий")
    void shouldReturnCategoryColors() {
        // When
        CategoryColorsDto result = categoryService.getCategoryColors();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getBackgroundColors()).containsExactly("#FF0000", "#00FF00", "#0000FF");
        assertThat(result.getTextColors()).containsExactly("#FFFFFF", "#000000");
    }
}