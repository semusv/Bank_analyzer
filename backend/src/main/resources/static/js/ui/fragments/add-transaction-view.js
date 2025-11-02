import {
    getLocalDateTimeString,
    showSuccessMessage,
    formatCurrency,
    showApiErrors
} from "../../modules/utils.js";
import {
    createTransaction
} from "../../modules/api/transactions-api.js";



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

export function populateAddTransactionModal(categoriesCache, cardsCache) {

    // Заполнить выбор категорий
    const categorySelect = document.getElementById('addCategoryId');
    categorySelect.innerHTML = '<option value="">Не выбрано</option>';
    categoriesCache.forEach(category => {
        const option = document.createElement('option');
        option.value = category.id;
        option.textContent = category.name;
        categorySelect.appendChild(option);
    });

    // Заполнить текущее время
    if (document.getElementById('addOperationTime')) {
        document.getElementById('addOperationTime').value = getLocalDateTimeString();
    }

    // Заполнить выбор карт
    const cardSelect = document.getElementById('addCardId');
    cardSelect.innerHTML = '<option value="">Не выбрано</option>';
    cardsCache.forEach(card => {
        const option = document.createElement('option');
        option.value = card.id;
        option.textContent = `${card.cardName} (****${card.lastFourDigits}) - ${card.currency.code}`;
        cardSelect.appendChild(option);
    });

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