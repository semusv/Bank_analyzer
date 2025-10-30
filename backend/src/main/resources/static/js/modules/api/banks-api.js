import { handleApiResponse } from "../utils.js";

const bankThemeCache = {};


export async function fetchBanks() {

    const response = await fetch(`/api/bank`, {
        headers: {
            'Accept': 'application/json'
        }
    });

    return await handleApiResponse(response);
}

export async function fetchBankTheme(bankNameOrCode) {
    if (bankThemeCache[bankNameOrCode])
        return bankThemeCache[bankNameOrCode];

    const response = await fetch(`/api/bank-themes/${encodeURIComponent(bankNameOrCode)}`, {
        headers: {
            'Accept': 'application/json'
        }
    });
    const theme = await handleApiResponse(response);
    bankThemeCache[bankNameOrCode] = theme;
    return theme;
}
