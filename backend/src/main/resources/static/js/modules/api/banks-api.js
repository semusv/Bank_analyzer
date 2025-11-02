import { handleApiResponse } from "../utils.js";

const bankThemeCache = new Map();


export async function fetchBanks() {

    const response = await fetch(`/api/bank`, {
        headers: {
            'Accept': 'application/json'
        }
    });

    return await handleApiResponse(response);
}

// export async function fetchBankTheme(bankNameOrCode) {
//     if (bankThemeCache[bankNameOrCode])
//         return bankThemeCache[bankNameOrCode];

//     const response = await fetch(`/api/bank-themes/${encodeURIComponent(bankNameOrCode)}`, {
//         headers: {
//             'Accept': 'application/json'
//         }
//     });
//     const theme = await handleApiResponse(response);
//     bankThemeCache[bankNameOrCode] = theme;
//     return theme;
// }

export async function fetchBankTheme(bankNameOrCode) {
    // Если уже есть запрос (промис) для этого банка, возвращаем его
    if (bankThemeCache.has(bankNameOrCode)) {
        return bankThemeCache.get(bankNameOrCode);
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
            // При ошибке удаляем из кеша, чтобы можно было повторить
            bankThemeCache.delete(bankNameOrCode);
            throw error;
        }
    })();

    // Сохраняем ПРОМИС в кеш, а не результат
    bankThemeCache.set(bankNameOrCode, fetchPromise);

    return fetchPromise;
}
