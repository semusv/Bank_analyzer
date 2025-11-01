# Backend Endpoints для страницы "Категории"

## Обзор

Данный документ описывает необходимые REST API эндпоинты для реализации функционала управления категориями в приложении Bank Analyzer.

## Требуемые эндпоинты

### 1. Получить все категории пользователя

**Эндпоинт:** `GET /api/category`

**Описание:** Возвращает список всех категорий, созданных текущим пользователем.

**Параметры:** Нет

**Аутентификация:** Требуется (через @AuthenticationPrincipal User)

**Ответ:**
```json
[
  {
    "id": 1,
    "name": "Продукты",
    "backgroundColor": "#FFE4E1",
    "textColor": "#333333",
    "userId": 1
  },
  {
    "id": 2,
    "name": "Транспорт",
    "backgroundColor": "#E6E6FA",
    "textColor": "#000000",
    "userId": 1
  }
]
```

**Статус коды:**
- 200 OK - успешное получение списка категорий
- 401 Unauthorized - пользователь не аутентифицирован

---

### 2. Получить категорию по ID

**Эндпоинт:** `GET /api/category/{id}`

**Описание:** Возвращает детальную информацию о категории по её ID.

**Параметры пути:**
- `id` (Long) - ID категории

**Аутентификация:** Требуется

**Ответ:**
```json
{
  "id": 1,
  "name": "Продукты",
  "backgroundColor": "#FFE4E1",
  "textColor": "#333333",
  "userId": 1
}
```

**Статус коды:**
- 200 OK - категория найдена
- 404 Not Found - категория не найдена
- 401 Unauthorized - пользователь не аутентифицирован
- 403 Forbidden - категория принадлежит другому пользователю

---

### 3. Создать новую категорию

**Эндпоинт:** `POST /api/category`

**Описание:** Создает новую категорию для текущего пользователя.

**Аутентификация:** Требуется

**Тело запроса:**
```json
{
  "name": "Продукты",
  "backgroundColor": "#FFE4E1",
  "textColor": "#333333"
}
```

**Поля запроса:**
- `name` (String, обязательное) - название категории (не пустое, макс 100 символов)
- `backgroundColor` (String, обязательное) - цвет фона в формате HEX (#RRGGBB)
- `textColor` (String, обязательное) - цвет текста в формате HEX (#RRGGBB)

**Ответ:**
```json
{
  "id": 1,
  "name": "Продукты",
  "backgroundColor": "#FFE4E1",
  "textColor": "#333333",
  "userId": 1
}
```

**Статус коды:**
- 201 Created - категория успешно создана
- 400 Bad Request - некорректные данные в запросе
- 401 Unauthorized - пользователь не аутентифицирован
- 409 Conflict - категория с таким именем уже существует у пользователя

**Валидация:**
- Название не должно быть пустым
- Название должно быть уникальным для пользователя
- Цвета должны быть в формате HEX (#RRGGBB)

---

### 4. Обновить категорию

**Эндпоинт:** `PUT /api/category/{id}`

**Описание:** Обновляет существующую категорию.

**Параметры пути:**
- `id` (Long) - ID категории

**Аутентификация:** Требуется

**Тело запроса:**
```json
{
  "name": "Продукты и напитки",
  "backgroundColor": "#FFB6C1",
  "textColor": "#FFFFFF"
}
```

**Поля запроса:**
- `name` (String, обязательное) - новое название категории
- `backgroundColor` (String, обязательное) - новый цвет фона
- `textColor` (String, обязательное) - новый цвет текста

**Ответ:**
```json
{
  "id": 1,
  "name": "Продукты и напитки",
  "backgroundColor": "#FFB6C1",
  "textColor": "#FFFFFF",
  "userId": 1
}
```

**Статус коды:**
- 200 OK - категория успешно обновлена
- 400 Bad Request - некорректные данные в запросе
- 404 Not Found - категория не найдена
- 401 Unauthorized - пользователь не аутентифицирован
- 403 Forbidden - категория принадлежит другому пользователю
- 409 Conflict - категория с таким именем уже существует у пользователя

---

### 5. Удалить категорию

**Эндпоинт:** `DELETE /api/category/{id}`

**Описание:** Удаляет категорию. Все транзакции, связанные с этой категорией, должны остаться без категории (categoryId = null).

**Параметры пути:**
- `id` (Long) - ID категории

**Аутентификация:** Требуется

**Ответ:** Нет тела ответа

**Статус коды:**
- 204 No Content - категория успешно удалена
- 404 Not Found - категория не найдена
- 401 Unauthorized - пользователь не аутентифицирован
- 403 Forbidden - категория принадлежит другому пользователю

**Бизнес-логика:**
- При удалении категории все связанные транзакции должны получить categoryId = null
- Не должно быть каскадного удаления транзакций

---

### 6. Получить доступные цвета для категорий

**Эндпоинт:** `GET /api/category/colors`

**Описание:** Возвращает предустановленные цвета для фона и текста категорий. Этот эндпоинт опциональный - фронтенд имеет дефолтные цвета, но может получить их с бэкенда для централизованного управления.

**Аутентификация:** Требуется

**Ответ:**
```json
{
  "backgroundColors": [
    "#FFE4E1",
    "#E6E6FA",
    "#F0E68C",
    "#B0E0E6",
    "#FFB6C1",
    "#DDA0DD",
    "#F5DEB3",
    "#98FB98",
    "#FFE4B5",
    "#D8BFD8"
  ],
  "textColors": [
    "#333333",
    "#000000",
    "#FFFFFF",
    "#2C3E50",
    "#8B4513",
    "#4A4A4A",
    "#1A1A1A",
    "#F0F0F0",
    "#5D4E37",
    "#696969"
  ]
}
```

**Статус коды:**
- 200 OK - цвета успешно получены
- 401 Unauthorized - пользователь не аутентифицирован

---

## Структура данных

### CategoryDto

```java
public class CategoryDto {
    private Long id;
    
    @NotBlank(message = "Название категории не может быть пустым")
    @Size(max = 100, message = "Название категории не может быть длиннее 100 символов")
    private String name;
    
    @NotBlank(message = "Цвет фона обязателен")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Цвет фона должен быть в формате HEX (#RRGGBB)")
    private String backgroundColor;
    
    @NotBlank(message = "Цвет текста обязателен")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Цвет текста должен быть в формате HEX (#RRGGBB)")
    private String textColor;
    
    private Long userId;
}
```

### CategoryColorsDto

```java
public class CategoryColorsDto {
    private List<String> backgroundColors;
    private List<String> textColors;
}
```

---

## Контроллер CategoryController

### Структура контроллера

```java
@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {
    
    private final CategoryService categoryService;
    
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CategoryDto> getAll(@AuthenticationPrincipal User user) {
        return categoryService.getCategoriesForUser(user.getId());
    }
    
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto getById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        return categoryService.getCategoryById(id, user.getId());
    }
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto create(
            @Valid @RequestBody CategoryDto categoryDto,
            @AuthenticationPrincipal User user
    ) {
        return categoryService.createCategory(categoryDto, user.getId());
    }
    
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDto categoryDto,
            @AuthenticationPrincipal User user
    ) {
        return categoryService.updateCategory(id, categoryDto, user.getId());
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        categoryService.deleteCategory(id, user.getId());
    }
    
    @GetMapping("/colors")
    @ResponseStatus(HttpStatus.OK)
    public CategoryColorsDto getColors() {
        return categoryService.getAvailableColors();
    }
}
```

---

## Сервисный слой

### CategoryService (интерфейс)

```java
public interface CategoryService {
    List<CategoryDto> getCategoriesForUser(Long userId);
    CategoryDto getCategoryById(Long categoryId, Long userId);
    CategoryDto createCategory(CategoryDto categoryDto, Long userId);
    CategoryDto updateCategory(Long categoryId, CategoryDto categoryDto, Long userId);
    void deleteCategory(Long categoryId, Long userId);
    CategoryColorsDto getAvailableColors();
}
```

---

## Модель данных

### Обновление модели Category

Необходимо добавить поля `backgroundColor` и `textColor` в модель `Category`:

```java
@Entity
@Table(name = "categories")
public class Category extends AbstractBaseEntity {
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "color") // существующее поле для обратной совместимости
    private String color;
    
    @Column(name = "background_color", nullable = false, length = 7)
    private String backgroundColor;
    
    @Column(name = "text_color", nullable = false, length = 7)
    private String textColor;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    // getters and setters
}
```

---

## Миграция базы данных (Liquibase)

```yaml
databaseChangeLog:
  - changeSet:
      id: add-category-color-fields
      author: developer
      changes:
        - addColumn:
            tableName: categories
            columns:
              - column:
                  name: background_color
                  type: varchar(7)
                  defaultValue: '#6c757d'
                  constraints:
                    nullable: false
              - column:
                  name: text_color
                  type: varchar(7)
                  defaultValue: '#FFFFFF'
                  constraints:
                    nullable: false
        - sql:
            sql: >
              UPDATE categories 
              SET background_color = COALESCE(color, '#6c757d'),
                  text_color = '#FFFFFF'
              WHERE background_color IS NULL;
```

---

## Обработка ошибок

Все эндпоинты должны возвращать стандартизированные ошибки в следующем формате:

```json
{
  "timestamp": "2025-11-01T09:50:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Название категории не может быть пустым",
  "path": "/api/category"
}
```

Или для множественных ошибок валидации:

```json
{
  "timestamp": "2025-11-01T09:50:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "errors": [
    {
      "field": "name",
      "message": "Название категории не может быть пустым"
    },
    {
      "field": "backgroundColor",
      "message": "Цвет фона должен быть в формате HEX (#RRGGBB)"
    }
  ],
  "path": "/api/category"
}
```

---

## Примечания по реализации

1. **Безопасность:**
   - Все эндпоинты должны проверять, что пользователь имеет доступ только к своим категориям
   - При получении/обновлении/удалении категории нужно проверять userId

2. **Валидация:**
   - Использовать аннотации валидации из javax.validation
   - Проверять формат HEX цветов регулярным выражением
   - Проверять уникальность названия категории в рамках пользователя

3. **Обратная совместимость:**
   - Поле `color` в модели Category следует оставить для обратной совместимости
   - При получении категории, если `backgroundColor` не установлен, использовать `color`
   - При создании/обновлении категории обновлять оба поля (`color` и `backgroundColor`)

4. **Производительность:**
   - Использовать индексы на поле user_id в таблице categories
   - Рассмотреть возможность кэширования списка категорий пользователя

5. **Транзакции:**
   - При удалении категории использовать транзакцию для обновления связанных транзакций
   - Обеспечить целостность данных

---

## Тестирование

### Примеры curl-команд для тестирования

#### Получить все категории
```bash
curl -X GET http://localhost:8080/api/category \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### Создать категорию
```bash
curl -X POST http://localhost:8080/api/category \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "Продукты",
    "backgroundColor": "#FFE4E1",
    "textColor": "#333333"
  }'
```

#### Обновить категорию
```bash
curl -X PUT http://localhost:8080/api/category/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "Продукты и напитки",
    "backgroundColor": "#FFB6C1",
    "textColor": "#FFFFFF"
  }'
```

#### Удалить категорию
```bash
curl -X DELETE http://localhost:8080/api/category/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### Получить цвета
```bash
curl -X GET http://localhost:8080/api/category/colors \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## Дополнительные возможности (опционально)

### 1. Сортировка категорий
Добавить возможность пользователю изменять порядок отображения категорий:

**Эндпоинт:** `PATCH /api/category/reorder`

**Тело запроса:**
```json
{
  "categoryIds": [3, 1, 2, 5, 4]
}
```

### 2. Статистика по категориям
Добавить информацию о количестве транзакций в каждой категории:

**Эндпоинт:** `GET /api/category/stats`

**Ответ:**
```json
[
  {
    "categoryId": 1,
    "categoryName": "Продукты",
    "transactionCount": 45,
    "totalAmount": -15678.50
  }
]
```

### 3. Импорт/Экспорт категорий
Позволить пользователям импортировать и экспортировать свои категории.

---

## Заключение

Данная спецификация описывает полный набор эндпоинтов для управления категориями в приложении Bank Analyzer. Реализация этих эндпоинтов позволит пользователям:

- Создавать персональные категории для классификации транзакций
- Настраивать визуальное отображение категорий (цвета фона и текста)
- Редактировать и удалять существующие категории
- Использовать предустановленные пастельные цвета для удобства

Фронтенд-часть уже реализована и готова к интеграции с бэкендом после создания описанных эндпоинтов.
