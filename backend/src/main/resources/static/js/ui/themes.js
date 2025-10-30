import { fetchBankTheme } from "../modules/api/banks-api.js";

export async function getBankColorsForElem(bankCode, txnElem) {
    try {
        const theme = await fetchBankTheme(bankCode);
        if (!txnElem || !theme.primaryColor) return;
        const gradient = `linear-gradient(90deg, ${theme.primaryColor} 0%, ${theme.secondaryColor ?? theme.primaryColor} 100%)`;
        txnElem.style.background = gradient;
        txnElem.style.color = theme.textColor || "#fff";
    } catch (error) {
        console.log(`Тема для ${bankCode} не загружена:`, error);
    }
}