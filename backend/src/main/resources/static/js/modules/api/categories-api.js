import { handleApiResponse } from "../utils.js";

const categoriesCache = new Map();
const CACHE_TTL = 10 * 60 * 1000; // 10 минут

// Вспомогательные функции для работы с кешем
function getFromCache(key) {
    const cached = categoriesCache.get(key);
    if (cached && (Date.now() - cached.timestamp) < CACHE_TTL) {
        return cached.data;
    }
    // Удаляем устаревшую запись
    categoriesCache.delete(key);
    return null;
}

function setToCache(key, data) {
    categoriesCache.set(key, {
        data: data,
        timestamp: Date.now()
    });
}

function clearCache() {
    categoriesCache.clear();
}

function invalidateCategory(categoryId) {
    // Удаляем конкретную категорию
    categoriesCache.delete(`category-${categoryId}`);
    // Инвалидируем кеш списка всех категорий
    categoriesCache.delete('all-categories');
}

function invalidateAllCategories() {
    // Удаляем все записи, связанные с категориями
    for (const key of categoriesCache.keys()) {
        if (key.startsWith('category-') || key === 'all-categories' || key === 'category-colors') {
            categoriesCache.delete(key);
        }
    }
}

// Периодическая очистка устаревшего кеша
setInterval(() => {
    const now = Date.now();
    for (const [key, value] of categoriesCache.entries()) {
        if (now - value.timestamp >= CACHE_TTL) {
            categoriesCache.delete(key);
        }
    }
}, 60 * 1000); // Проверка каждую минуту

export async function fetchCategories() {
    const cacheKey = 'all-categories';

    // Пробуем получить из кеша
    const cached = getFromCache(cacheKey);
    if (cached) {
        return cached;
    }

    // Если уже есть активный промис для этого запроса, возвращаем его
    const pendingPromiseKey = `pending-${cacheKey}`;
    if (categoriesCache.has(pendingPromiseKey)) {
        return categoriesCache.get(pendingPromiseKey);
    }

    // Создаем новый промис запроса
    const fetchPromise = (async () => {
        try {
            const response = await fetch('/api/category', {
                headers: {
                    'Accept': 'application/json'
                }
            });

            const categories = await handleApiResponse(response);

            // Сохраняем результат в кеш
            setToCache(cacheKey, categories);

            // Также кешируем каждую категорию отдельно для быстрого доступа по ID
            if (Array.isArray(categories)) {
                categories.forEach(category => {
                    setToCache(`category-${category.id}`, category);
                });
            }

            return categories;
        } finally {
            // Удаляем промис из кеша после завершения
            categoriesCache.delete(pendingPromiseKey);
        }
    })();

    // Сохраняем промис в кеш
    categoriesCache.set(pendingPromiseKey, fetchPromise);

    return fetchPromise;
}

export async function fetchCategoryById(categoryId) {
    const cacheKey = `category-${categoryId}`;

    // Пробуем получить из кеша
    const cached = getFromCache(cacheKey);
    if (cached) {
        return cached;
    }

    // Если уже есть активный промис для этого запроса, возвращаем его
    const pendingPromiseKey = `pending-${cacheKey}`;
    if (categoriesCache.has(pendingPromiseKey)) {
        return categoriesCache.get(pendingPromiseKey);
    }

    // Создаем новый промис запроса
    const fetchPromise = (async () => {
        try {
            const response = await fetch(`/api/category/${categoryId}`, {
                headers: {
                    'Accept': 'application/json'
                }
            });

            const category = await handleApiResponse(response);

            // Сохраняем результат в кеш
            setToCache(cacheKey, category);

            return category;
        } finally {
            // Удаляем промис из кеша после завершения
            categoriesCache.delete(pendingPromiseKey);
        }
    })();

    // Сохраняем промис в кеш
    categoriesCache.set(pendingPromiseKey, fetchPromise);

    return fetchPromise;
}

export async function createCategory(categoryData) {
    // Инвалидируем кеш перед выполнением запроса
    invalidateAllCategories();

    const response = await fetch('/api/category', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify(categoryData)
    });

    const newCategory = await handleApiResponse(response);

    return newCategory;
}

export async function updateCategory(categoryId, categoryData) {
    // Инвалидируем кеш перед выполнением запроса
    invalidateCategory(categoryId);

    const response = await fetch(`/api/category/${categoryId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify(categoryData)
    });

    const updatedCategory = await handleApiResponse(response);

    return updatedCategory;
}

export async function deleteCategory(categoryId) {
    // Инвалидируем кеш перед выполнением запроса
    invalidateCategory(categoryId);

    const response = await fetch(`/api/category/${categoryId}`, {
        method: 'DELETE',
        headers: {
            'Accept': 'application/json'
        }
    });

    const result = await handleApiResponse(response);

    return result;
}

export async function fetchCategoryColors() {
    const cacheKey = 'category-colors';

    // Пробуем получить из кеша
    const cached = getFromCache(cacheKey);
    if (cached) {
        return cached;
    }

    // Если уже есть активный промис для этого запроса, возвращаем его
    const pendingPromiseKey = `pending-${cacheKey}`;
    if (categoriesCache.has(pendingPromiseKey)) {
        return categoriesCache.get(pendingPromiseKey);
    }

    // Создаем новый промис запроса
    const fetchPromise = (async () => {
        try {
            const response = await fetch('/api/category/colors', {
                headers: {
                    'Accept': 'application/json'
                }
            });

            const colors = await handleApiResponse(response);

            // Сохраняем результат в кеш
            setToCache(cacheKey, colors);

            return colors;
        } finally {
            // Удаляем промис из кеша после завершения
            categoriesCache.delete(pendingPromiseKey);
        }
    })();

    // Сохраняем промис в кеш
    categoriesCache.set(pendingPromiseKey, fetchPromise);

    return fetchPromise;
}

// Вспомогательная функция для получения категории из кеша (для использования в renderTransactions)
export function getCategoryById(categoryId) {
    return getFromCache(`category-${categoryId}`);
}

// Функции для управления кешем (можно использовать для принудительного сброса)
export function clearCategoriesCache() {
    clearCache();
}

export function invalidateCategoriesCache() {
    invalidateAllCategories();
}