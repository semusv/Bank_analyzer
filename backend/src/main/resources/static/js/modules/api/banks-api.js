import { handleApiResponse } from "../utils.js";

const bankThemePromiseCache = new Map();
const CACHE_TTL = 5 * 60 * 1000; // 5 минут в миллисекундах

export async function fetchBanks() {
    const response = await fetch(`/api/bank`, {
        headers: {
            'Accept': 'application/json'
        }
    });
    return await handleApiResponse(response);
}

export async function fetchBankById(bankId) {
    const response = await fetch(`/api/bank/${bankId}`, {
        headers: {
            'Accept': 'application/json'
        }
    });
    return await handleApiResponse(response);
}


export async function fetchBankTheme(bankNameOrCode) {
    const now = Date.now();

    // Проверяем есть ли запись в кеше и не устарела ли она
    const cached = bankThemePromiseCache.get(bankNameOrCode);
    if (cached && (now - cached.timestamp) < CACHE_TTL) {
        return cached.promise;
    }

    // Создаем новый промис запроса
    const fetchPromise = (async () => {
        try {
            const response = await fetch(`/api/bank-themes/${encodeURIComponent(bankNameOrCode)}`, {
                headers: {
                    'Accept': 'application/json'
                }
            });
            const theme = await handleApiResponse(response);
            return theme;
        } catch (error) {
            // При ошибке удаляем из кеша
            bankThemePromiseCache.delete(bankNameOrCode);
            throw error;
        }
    })();

    // Сохраняем промис с временной меткой
    bankThemePromiseCache.set(bankNameOrCode, {
        promise: fetchPromise,
        timestamp: now
    });

    return fetchPromise;
}

// Функция для очистки устаревших записей (можно вызывать периодически)
export function clearExpiredBankThemeCache() {
    const now = Date.now();
    for (const [key, value] of bankThemePromiseCache.entries()) {
        if (now - value.timestamp >= CACHE_TTL) {
            bankThemePromiseCache.delete(key);
        }
    }
}

// Функция для принудительной очистки кеша
export function clearBankThemeCache() {
    bankThemePromiseCache.clear();
}