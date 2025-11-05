import {
    getLocalDateTimeString,
    showSuccessMessage,
    formatCurrency,
    showApiErrors
} from "../../modules/utils.js";
import {
    createTransaction
} from "../../modules/api/transactions-api.js";
import { setBankColorsForElem } from "../themes.js";


export async function handleAddTransaction(event) {
    event.preventDefault();
    const form = event.target;
    const formData = new FormData(form);

    const transactionData = {
        description: formData.get('description'),
        amount: Number.parseFloat(formData.get('amount')),
        operationTime: formData.get('operationTime'),
        categoryId: formData.get('categoryId') ? Number.parseInt(formData.get('categoryId')) : null,
        cardId: formData.get('cardId') ? Number.parseInt(formData.get('cardId')) : null,
        operationType: formData.get("operationType")
    };

    try {
        await createTransaction(transactionData);
        showSuccessMessage('Транзакция успешно добавлена!');

        if (typeof bootstrap !== 'undefined') {
            const modalElement = document.getElementById('addTransactionModal');
            const modal = bootstrap.Modal.getInstance(modalElement) || new bootstrap.Modal(modalElement);
            modal.hide();
        }

        event.target.reset();

        document.dispatchEvent(new CustomEvent('transactionAddSuccess', {
            //detail: transaction
        }));


    } catch (error) {
        console.error('Failed create transaction', error);
        showApiErrors(error);
    }
}
function fillCategoryDropdown(categoriesCache) {
    const categoryDropdownMenu = document.getElementById('categoryDropdownMenu');
    const categoryDropdownBtn = document.getElementById('categoryDropdownBtn');
    const categoryHiddenInput = document.getElementById('addCategoryId');

    categoriesCache.forEach(category => {
        const item = document.createElement('a');
        item.className = 'dropdown-item d-flex align-items-center';
        item.href = '#';
        item.innerHTML = `
        <span class="badge me-2" style="background-color: ${category.color}; color: ${category.textColor};">${category.name}</span>`;
        item.dataset.value = category.id;
        categoryDropdownMenu.appendChild(item);
    });

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

function fillCardDropdown(cardCache) {
    const cardDropdownMenu = document.getElementById('cardDropdownMenu');
    const cardDropdownBtn = document.getElementById('cardDropdownBtn');
    const cardHiddenInput = document.getElementById('addCardId');

    cardCache.forEach(card => {
        const item = document.createElement('a');
        item.className = 'dropdown-item d-flex align-items-center';
        item.href = '#';
        item.dataset.value = card.id;

        const badge = document.createElement('span');
        badge.classList.add('badge', 'me-2');
        badge.textContent = `${card.cardName} (****${card.lastFourDigits}) - ${card.currency.code}`;
        setBankColorsForElem(card.bankCode, badge)

        item.appendChild(badge);
        cardDropdownMenu.appendChild(item);
    });

    // Обработчик выбора
    cardDropdownMenu.addEventListener('click', function (e) {
        e.preventDefault();
        const target = e.target.closest('.dropdown-item');
        if (target) {
            const value = target.dataset.value;
            const text = target.textContent.trim();

            cardHiddenInput.value = value;
            cardDropdownBtn.textContent = text || 'Выберите категорию';

            const childDropdownBtn = target.querySelector('.badge');
            cardDropdownBtn.style.background = childDropdownBtn ? childDropdownBtn.style.background : '';
            cardDropdownBtn.style.color = childDropdownBtn ? childDropdownBtn.style.color : '';
        }
    });
}
export function populateAddTransactionModal(categoriesCache, cardsCache) {
    fillCategoryDropdown(categoriesCache);
    fillCardDropdown(cardsCache);

    // Заполнить текущее время
    if (document.getElementById('addOperationTime')) {
        document.getElementById('addOperationTime').value = getLocalDateTimeString();
    }



    // // Заполнить выбор карт
    // const cardSelect = document.getElementById('addCardId');
    // cardSelect.innerHTML = '<option value="">Не выбрано</option>';
    // cardsCache.forEach(card => {
    //     const option = document.createElement('option');
    //     option.value = card.id;
    //     option.textContent = `${card.cardName} (****${card.lastFourDigits}) - ${card.currency.code}`;

    //     setBankColorsForElem(card.bankCode, option)
    //     cardSelect.appendChild(option);
    // });

    // изменяем цвет суммы в зависимости от типа операции
    document.querySelectorAll('input[type="radio"][name="operationType"]')
        .forEach(radio => {
            radio.addEventListener('change', () => {
                changeAddAmountColor(radio);
            });
        });

    document.getElementById('addCardId').addEventListener('change', () => {
        cardsCache.forEach(card => {
            if (card.id === Number.parseInt(document.getElementById('addCardId').value)) {
                document.getElementById('addAmount').textContent = formatCurrency(document.getElementById('addAmount').value, card.currency.code);
            }
        });
    });
}

function changeAddAmountColor(radio) {
    const amountInput = document.getElementById('addAmount');
    amountInput.classList.remove('text-danger', 'text-success', 'text-info');

    if (radio.value == 0) {
        amountInput.classList.add('text-danger');
    } else if (radio.value == 1) {
        amountInput.classList.add('text-success');
    } else if (radio.value == 2) {
        amountInput.classList.add('text-info');
    }
}