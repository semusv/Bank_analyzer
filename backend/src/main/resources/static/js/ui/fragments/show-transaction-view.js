import {
    formatCurrency,
    formatDateTime
} from "../../modules/utils.js";
import {
    setBankColorsForElem
} from "../themes.js";


export function populateTransactionDetailsModal(transaction, viewMode = true) {
    document.getElementById('detailDescription').textContent = transaction.description;

    // Сумма операции
    document.getElementById('detailAmount').textContent = formatCurrency(transaction.amount, transaction.currency.code);
    document.getElementById('detailAmount').className =
        `amount ${transaction.amount >= 0 ? 'text-success' : 'text-danger'}`;


    document.getElementById('detailDateTime').textContent = formatDateTime(transaction.operationTime);

    // Card information with badge
    const cardElement = document.getElementById('detailCard');
    if (transaction.card) {
        cardElement.innerHTML = `<span class="badge bg-primary">**** ${transaction.card.lastFourDigits}</span>`;
        cardElement.innerHTML += ` <span class="text-muted">${transaction.card.cardName}</span>`;
    } else {
        cardElement.textContent = 'Не указана';
    }

    // Bank information with badge
    const bankElement = document.getElementById('detailBank');
    if (transaction.bank) {
        const bankElementBadge = document.getElementById('detailBankBadge');
        bankElementBadge.innerHTML = transaction.bank.name;
        setBankColorsForElem(transaction.bank.bankCode, bankElementBadge);
    } else if (transaction.card) {
        bankElement.textContent = 'Не указан';
    } else {
        bankElement.textContent = 'Не указан';
    }


    // Category with color
    const categoryElement = document.getElementById('detailCategory');
    if (transaction.category) {
        const color = transaction.category.color || '#6c757d';
        const textColor = transaction.category.textcolor || '#6c757d';
        categoryElement.innerHTML = `<span class="badge" style="background-color: ${color}; color: ${textColor};">${transaction.category.name}</span>`;
    } else {
        categoryElement.textContent = 'Не указана';
    }

    document.getElementById('detailHide').checked = transaction.hide;
    document.getElementById('detailHideStatus').textContent = transaction.hide ? 'Скрыта из статистики' : 'Отображается в статистике';
    document.getElementById('detailHideStatus').className = transaction.hide ? 'text-warning' : 'text-success';

    const hideToggleBtn = document.getElementById('toggleHideBtn');
    hideToggleBtn.onclick = () => toggleHideTransactionById(transaction.id, !transaction.hide);
    hideToggleBtn.innerHTML = transaction.hide ?
        '<i class="fas fa-eye"></i> Показать в статистике' :
        '<i class="fas fa-eye-slash"></i> Скрыть из статистики';
    hideToggleBtn.className = transaction.hide ? 'btn btn-success' : 'btn btn-warning';

    const splitBtn = document.getElementById('splitTransactionBtn');
    splitBtn.onclick = () => openSplitTransactionModal(transaction.id, transaction.amount);

    if (viewMode) {
        document.getElementById('toggleHideBtn').style.display = 'none';
        document.getElementById('splitTransactionBtn').style.display = 'none';
    }

}
