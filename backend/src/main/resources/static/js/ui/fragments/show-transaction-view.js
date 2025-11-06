import {
    formatCurrency,
    formatDateTime,
    showApiErrors
} from "../../modules/utils.js";
import {
    setBankColorsForElem
} from "../themes.js";
import {
    updateTransaction
} from "../../modules/api/transactions-api.js";

export function populateTransactionDetailsModal(transaction, categoriesCache, viewMode = true) {
    document.getElementById('detailDescription').textContent = transaction.description;

    // Сумма операции
    document.getElementById('detailAmount').textContent = formatCurrency(transaction.amount, transaction.currency.code);
    document.getElementById('editAmount').value = Number.parseFloat(transaction.amount).toFixed(2);
    document.getElementById('detailAmount').classList.add(`${transaction.amount >= 0 ? 'text-success' : 'text-danger'}`);
    document.getElementById('editAmount').classList.add(`${transaction.amount >= 0 ? 'text-success' : 'text-danger'}`);

    //DateTime
    document.getElementById('detailDateTime').textContent = formatDateTime(transaction.operationTime);
    document.getElementById('editOperationTime').value = transaction.operationTime;

    // Card
    const cardElement = document.getElementById('detailCard');
    if (transaction.card) {
        cardElement.innerHTML = `<span class="badge bg-primary">**** ${transaction.card.lastFourDigits}</span>`;
        cardElement.innerHTML += ` <span class="text-muted">${transaction.card.cardName}</span>`;
    } else {
        cardElement.textContent = 'Не указана';
    }

    // Bank
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

    // Category
    const categoryElement = document.getElementById('detailCategory');
    if (transaction.category) {
        const color = transaction.category.color || '#6c757d';
        const textColor = transaction.category.textcolor || '#6c757d';
        categoryElement.innerHTML = `<span class="badge" style="background-color: ${color}; color: ${textColor};">${transaction.category.name}</span>`;
    } else {
        categoryElement.textContent = 'Не указана';
    }
    fillCategoryDropdown(categoriesCache, transaction.category.id);

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
        document.getElementById('editTransactionBtn').style.display = 'none';

    } else {
        setupEditHandlers(transaction.id);
        switchToEditMode(false);
    }

}
function fillCategoryDropdown(categoriesCache, categoryId) {
    if (!categoriesCache) {
        return;
    }

    const categoryDropdownMenu = document.getElementById('editCategoryDropdownMenu');
    const categoryDropdownBtn = document.getElementById('editCategoryDropdownBtn');
    const categoryHiddenInput = document.getElementById('editCategoryId');

    let defaultCategory = null;

    categoryDropdownMenu.innerHTML = '';

    categoriesCache.forEach(category => {
        const item = document.createElement('a');
        item.className = 'dropdown-item d-flex align-items-center';
        item.href = '#';
        item.innerHTML = `
        <span class="badge me-2" style="background-color: ${category.color}; color: ${category.textColor};">${category.name}</span>`;
        item.dataset.value = category.id;
        categoryDropdownMenu.appendChild(item);

        if (!defaultCategory || category.id === 0 || category.id === categoryId) {
            defaultCategory = category;
        }
    });

    if (defaultCategory) {
        categoryHiddenInput.value = defaultCategory.id;
        categoryDropdownBtn.textContent = defaultCategory.name;
        categoryDropdownBtn.style.backgroundColor = defaultCategory.color;
        categoryDropdownBtn.style.color = defaultCategory.textColor;
    }

    // Обработчик выбора
    categoryDropdownMenu.addEventListener('click', function (e) {
        e.preventDefault();
        const target = e.target.closest('.dropdown-item');
        if (target) {
            const value = target.dataset.value;
            const text = target.textContent.trim();

            categoryHiddenInput.value = value;
            categoryDropdownBtn.textContent = text || 'Выберите категорию';

            const childDropdownBtn = target.querySelector('.badge');
            categoryDropdownBtn.style.backgroundColor = childDropdownBtn ? childDropdownBtn.style.backgroundColor : '';
            categoryDropdownBtn.style.color = childDropdownBtn ? childDropdownBtn.style.color : '';
        }
    });
}


function setupEditHandlers(transactionId) {
    const editBtn = document.getElementById('editTransactionBtn');
    const saveBtn = document.getElementById('saveTransactionBtn');

    editBtn.onclick = () => {
        switchToEditMode(true);
    };

    saveBtn.onclick = () => {
        saveTransactionChanges(transactionId);
    };
}


function switchToEditMode(isEditMode) {
    // Скрываем/показываем элементы просмотра
    document.getElementById('viewAmountContainer').style.display = isEditMode ? 'none' : 'block';
    document.getElementById('viewDateTimeContainer').style.display = isEditMode ? 'none' : 'block';
    document.getElementById('viewCategoryContainer').style.display = isEditMode ? 'none' : 'block';

    // Показываем/скрываем элементы редактирования
    document.getElementById('editAmountContainer').style.display = isEditMode ? 'block' : 'none';
    document.getElementById('editDateTimeContainer').style.display = isEditMode ? 'block' : 'none';
    document.getElementById('editCategoryContainer').style.display = isEditMode ? 'block' : 'none';

    // Скрываем/показываем кнопки
    document.getElementById('editTransactionBtn').style.display = isEditMode ? 'none' : 'block';
    document.getElementById('saveTransactionBtn').style.display = isEditMode ? 'block' : 'none';
    document.getElementById('toggleHideBtn').style.display = isEditMode ? 'none' : 'block';
    document.getElementById('splitTransactionBtn').style.display = isEditMode ? 'none' : 'block';
}


async function saveTransactionChanges(transactionId) {
    const updatedData = {
        amount: Number.parseFloat(document.getElementById('editAmount').value),
        operationTime: document.getElementById('editOperationTime').value,
        categoryId: Number.parseInt(document.getElementById('editCategoryId').value)
    };

    try {
        await updateTransaction(transactionId, updatedData);

        document.dispatchEvent(new CustomEvent('transactionSaveSuccess', {
        }));
        if (typeof bootstrap !== 'undefined') {
            const modalElement = document.getElementById('transactionDetailsModal');
            const modal = bootstrap.Modal.getInstance(modalElement);
            if (modal) {
                modal.hide();
            }
        }
    } catch (error) {
        console.error('Failed update transaction', error);
        showApiErrors(error);
    }

};