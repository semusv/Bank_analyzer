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

export async function getBankColorsForBtn(bankCode, txnElem, isActive) {
    try {
        const theme = await fetchBankTheme(bankCode);

        if (!txnElem || !theme.primaryColor) return;

        // Сбрасываем стили
        txnElem.style.background = "";
        txnElem.style.color = "";
        txnElem.style.border = "";
        txnElem.style.borderWidth = "";
        txnElem.style.borderStyle = "";

        if (isActive) {
            const gradient = `linear-gradient(90deg, ${theme.primaryColor} 0%, ${theme.secondaryColor ?? theme.primaryColor} 100%)`;
            txnElem.style.background = gradient;
            txnElem.style.color = theme.textColor || getCssVariable('--dark');
            txnElem.style.border = `1px solid ${theme.primaryColor}`;
        } else {
            // Неактивное состояние: прозрачный фон, цветная граница
            txnElem.style.background = "transparent";
            txnElem.style.color = getCssVariable('--dark');
            txnElem.style.border = `1px solid ${theme.primaryColor}`;

            // Добавляем плавный переход
            txnElem.style.transition = "all 0.3s ease";
        }

    } catch (error) {
        console.log(`Тема для ${bankCode} не загружена:`, error);
    }
}

function getCssVariable(varName) {
    return getComputedStyle(document.documentElement)
        .getPropertyValue(varName)
        .trim();
}