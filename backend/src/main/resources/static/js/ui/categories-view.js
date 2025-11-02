import {
    fetchCategories,
    fetchCategoryById,
    createCategory,
    updateCategory,
    deleteCategory,
    fetchCategoryColors
} from "../modules/api/categories-api.js";
import {
    showErrorMessage,
    showSuccessMessage,
    showApiErrors
} from "../modules/utils.js";

document.addEventListener('DOMContentLoaded', init);

let categoriesCache = null;
let availableColors = null;

async function init() {
    try {
        await loadCategoryColors();
        await loadCategories();
        setupEventListeners();
    } catch (error) {
        console.error('Failed to initialize categories page:', error);
        showErrorMessage('Ошибка загрузки страницы: ' + error.message);
    }
}

async function loadCategoryColors() {
    try {
        availableColors = await fetchCategoryColors();
        console.log('Loaded category colors from backend:', availableColors);
    } catch (error) {
        console.warn('Failed to load colors from backend, using defaults:', error);
        availableColors = null;
    }
}

async function loadCategories() {
    try {
        categoriesCache = await fetchCategories();
        renderCategories(categoriesCache);
    } catch (error) {
        console.error('Failed to load categories:', error);
        renderEmptyState();
        throw error;
    }
}
function renderCategories(categories) {
    const container = document.getElementById('categoriesContainer');

    if (!categories || categories.length === 0) {
        renderEmptyState();
        return;
    }

    container.innerHTML = `
        <div class="list-group">
            ${categories.map(category => `
                <div class="col-12 col-sm-8 col-md-6 col-lg-6 list-group-item d-flex justify-content-between align-items-center">
                    <div class="d-flex align-items-center">
                        <span class="badge me-3 category-badge"
                            style="background-color: ${category.backgroundColor || category.color || '#6c757d'}; 
                                color: ${category.textColor || '#FFFFFF'};
                                min-width: 60px; text-align: center;">
                            ${category.name}
                        </span>
                    </div>
                    <div class="btn-group btn-group-sm">
                        <button class="btn btn-outline-primary"
                                onclick="openEditCategoryModal(${category.id})">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="btn btn-outline-danger"
                                onclick="deleteCategoryById(${category.id})">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </div>
            `).join('')}
        </div>
    `;
}

function renderEmptyState() {
    const container = document.getElementById('categoriesContainer');
    container.innerHTML = `
        <div class="col-12">
            <div class="text-center py-5">
                <i class="fas fa-tags fa-3x text-muted mb-3"></i>
                <h4 class="text-muted">Нет категорий</h4>
                <p class="text-muted">Создайте первую категорию для классификации транзакций</p>
                <button class="btn btn-primary" onclick="openAddCategoryModal()">
                    <i class="fas fa-plus"></i> Добавить категорию
                </button>
            </div>
        </div>
    `;
}

function setupEventListeners() {
    // Обработчик формы добавления категории
    const addCategoryForm = document.getElementById('addCategoryForm');
    if (addCategoryForm) {
        addCategoryForm.addEventListener('submit', handleAddCategory);
    }

    // Обработчик формы редактирования категории
    const editCategoryForm = document.getElementById('editCategoryForm');
    if (editCategoryForm) {
        editCategoryForm.addEventListener('submit', handleEditCategory);
    }

    // Обработчики изменения цветов для предпросмотра (добавление)
    const addNameInput = document.getElementById('addCategoryName');
    const addBgColorInput = document.getElementById('addBackgroundColor');
    const addTextColorInput = document.getElementById('addTextColor');

    if (addNameInput) {
        addNameInput.addEventListener('input', updateAddPreview);
    }
    if (addBgColorInput) {
        addBgColorInput.addEventListener('input', updateAddPreview);
    }
    if (addTextColorInput) {
        addTextColorInput.addEventListener('input', updateAddPreview);
    }

    // Обработчики изменения цветов для предпросмотра (редактирование)
    const editNameInput = document.getElementById('editCategoryName');
    const editBgColorInput = document.getElementById('editBackgroundColor');
    const editTextColorInput = document.getElementById('editTextColor');

    if (editNameInput) {
        editNameInput.addEventListener('input', updateEditPreview);
    }
    if (editBgColorInput) {
        editBgColorInput.addEventListener('input', updateEditPreview);
    }
    if (editTextColorInput) {
        editTextColorInput.addEventListener('input', updateEditPreview);
    }
}

async function handleAddCategory(event) {
    event.preventDefault();
    const form = event.target;
    const formData = new FormData(form);

    const categoryData = {
        name: formData.get('name'),
        color: formData.get('backgroundColor'),
        textColor: formData.get('textColor')
    };

    try {
        await createCategory(categoryData);
        showSuccessMessage('Категория успешно добавлена!');

        if (typeof bootstrap !== 'undefined') {
            const modalElement = document.getElementById('addCategoryModal');
            const modal = bootstrap.Modal.getInstance(modalElement) || new bootstrap.Modal(modalElement);
            modal.hide();
        }

        form.reset();
        await loadCategories();
    } catch (error) {
        console.error('Failed to create category:', error);
        showApiErrors(error);
    }
}

async function handleEditCategory(event) {
    event.preventDefault();
    const form = event.target;
    const formData = new FormData(form);

    const categoryId = formData.get('id');
    const categoryData = {
        name: formData.get('name'),
        color: formData.get('backgroundColor'),
        textColor: formData.get('textColor')
    };

    try {
        await updateCategory(categoryId, categoryData);
        showSuccessMessage('Категория успешно обновлена!');

        if (typeof bootstrap !== 'undefined') {
            const modalElement = document.getElementById('editCategoryModal');
            const modal = bootstrap.Modal.getInstance(modalElement) || new bootstrap.Modal(modalElement);
            modal.hide();
        }

        await loadCategories();
    } catch (error) {
        console.error('Failed to update category:', error);
        showApiErrors(error);
    }
}

function updateAddPreview() {
    const name = document.getElementById('addCategoryName').value || 'Название категории';
    const bgColor = document.getElementById('addBackgroundColor').value;
    const textColor = document.getElementById('addTextColor').value;

    const badge = document.getElementById('addPreviewBadge');
    badge.textContent = name;
    badge.style.backgroundColor = bgColor;
    badge.style.color = textColor;
}

function updateEditPreview() {
    const name = document.getElementById('editCategoryName').value || 'Название категории';
    const bgColor = document.getElementById('editBackgroundColor').value;
    const textColor = document.getElementById('editTextColor').value;

    const badge = document.getElementById('editPreviewBadge');
    badge.textContent = name;
    badge.style.backgroundColor = bgColor;
    badge.style.color = textColor;
}

function renderColorPicker(containerId, colors, inputId, type = 'background') {
    const container = document.getElementById(containerId);
    if (!container) return;

    const colorsToUse = colors;

    container.innerHTML = colorsToUse.map(color =>
        `<button type="button" class="color-option" 
            style="background-color: ${color}; color: ${color}; margin: 0.1rem; border: .25px solid #000"
            data-color="${color}"
            onclick="selectColor('${inputId}', '${color}', '${type}')">
            <span style="font-weight: bold;">A</span>
        </button>`
    ).join('');
}

globalThis.selectColor = function (inputId, color, type) {
    const input = document.getElementById(inputId);
    if (input) {
        input.value = color;
        input.dispatchEvent(new Event('input'));
    }
};

globalThis.openAddCategoryModal = function () {
    if (typeof bootstrap === 'undefined') {
        console.error('Bootstrap is not loaded');
        showErrorMessage('Ошибка: Bootstrap не загружен. Пожалуйста, обновите страницу.');
        return;
    }
    // Рендерим палитры цветов
    const bgColors = availableColors.backgroundColors;
    const textColors = availableColors.textColors;

    renderColorPicker('backgroundColorPicker', bgColors, 'addBackgroundColor', 'background');
    renderColorPicker('textColorPicker', textColors, 'addTextColor', 'text');

    // Сбросить форму и установить значения по умолчанию
    document.getElementById('addCategoryForm').reset();
    document.getElementById('addBackgroundColor').value = '#FFE4E1';
    document.getElementById('addTextColor').value = '#333333';
    updateAddPreview();

    const modal = new bootstrap.Modal(document.getElementById('addCategoryModal'));
    modal.show();
};

globalThis.openEditCategoryModal = async function (categoryId) {
    if (typeof bootstrap === 'undefined') {
        console.error('Bootstrap is not loaded');
        return;
    }

    try {
        const category = await fetchCategoryById(categoryId);

        // Заполнить форму данными категории
        document.getElementById('editCategoryId').value = category.id;
        document.getElementById('editCategoryName').value = category.name;
        document.getElementById('editBackgroundColor').value = category.backgroundColor || category.color || '#6c757d';
        document.getElementById('editTextColor').value = category.textColor || '#FFFFFF';

        // Рендерим палитры цветов
        const bgColors = availableColors?.backgroundColors;
        const textColors = availableColors?.textColors;

        renderColorPicker('editBackgroundColorPicker', bgColors, 'editBackgroundColor', 'background');
        renderColorPicker('editTextColorPicker', textColors, 'editTextColor', 'text');

        updateEditPreview();

        const modal = new bootstrap.Modal(document.getElementById('editCategoryModal'));
        modal.show();
    } catch (error) {
        console.error('Failed to load category details:', error);
        showApiErrors(error);
    }
};

globalThis.deleteCategoryById = async function (categoryId) {
    if (!confirm('Вы уверены, что хотите удалить эту категорию? Все транзакции с этой категорией останутся без категории.')) {
        return;
    }

    try {
        await deleteCategory(categoryId);
        showSuccessMessage('Категория успешно удалена!');
        await loadCategories();
    } catch (error) {
        console.error('Failed to delete category:', error);
        showApiErrors(error);
    }
};
