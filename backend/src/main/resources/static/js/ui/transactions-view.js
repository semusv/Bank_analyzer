import {
    fetchTransactions,
    fetchTransactionById,
    createTransaction,
    deleteTransaction,
    toggleHideTransaction,
    splitTransaction
} from "../modules/api/transactions-api.js";
import { fetchCards } from "../modules/api/cards-api.js";
import { fetchCategories } from "../modules/api/categories-api.js";
import { fetchBanks } from "../modules/api/banks-api.js";
import { fetchCurrencies } from "../modules/api/currency-api.js";
import { showErrorMessage, showSuccessMessage, formatCurrency } from "../modules/utils.js";

document.addEventListener('DOMContentLoaded', init);

let currentFilters = {};
let categoriesCache = null;
let cardsCache = null;
let banksCache = null;
let currenciesCache = null;
let currentPage = 0;
let totalPages = 0;
let pageSize = 20;

async function init() {
    try {
        await Promise.all([
            loadCategories(),
            loadCards(),
            loadBanks(),
            loadCurrencies()
        ]);
        populateFilters();
        await loadTransactions();
        setupEventListeners();
    } catch (error) {
        console.error('Failed to initialize transactions page:', error);
        showErrorMessage('Ошибка загрузки страницы: ' + error.message);
    }
}

async function loadCategories() {
    try {
        categoriesCache = await fetchCategories();
        return categoriesCache;
    } catch (error) {
        console.error('Failed to load categories:', error);
        return [];
    }
}

async function loadCards() {
    try {
        cardsCache = await fetchCards();
        return cardsCache;
    } catch (error) {
        console.error('Failed to load cards:', error);
        return [];
    }
}

async function loadBanks() {
    try {
        banksCache = await fetchBanks();
        return banksCache;
    } catch (error) {
        console.error('Failed to load banks:', error);
        return [];
    }
}

async function loadCurrencies() {
    try {
        currenciesCache = await fetchCurrencies();
        return currenciesCache;
    } catch (error) {
        console.error('Failed to load currencies:', error);
        return [];
    }
}

async function loadTransactions(page = 0) {
    try {
        currentPage = page;
        const filtersWithPagination = {
            ...currentFilters,
            page: currentPage,
            size: pageSize
        };

        const response = await fetchTransactions(filtersWithPagination);

        // Handle paginated response
        const transactions = response.content || response;
        totalPages = response.totalPages || 1;

        renderTransactions(transactions);
        renderPagination();
    } catch (error) {
        console.error('Failed to load transactions:', error);
        showErrorMessage('Ошибка загрузки транзакций: ' + error.message);
        renderEmptyState();
    }
}

function renderTransactions(transactions) {
    const container = document.getElementById('transactionsContainer');

    if (!transactions || transactions.length === 0) {
        renderEmptyState();
        return;
    }

    container.innerHTML = transactions.map(transaction => `
        <div class="card transaction-card mb-3 ${transaction.hide ? 'opacity-50' : ''}" data-transaction-id="${transaction.id}" data-bank="${transaction.bank.name}">
            <div class="card-body">
                <div class="row align-items-center" ">
                    <div class="col-12 col-md-6 col-lg-6">
                        <div class="transaction-info">
                            <h6 class="mb-1">
                                    ${transaction.description}
                                ${transaction.hide ? '<span class="badge bg-secondary ms-2">Скрыто</span>' : ''}
                            </h6>
                            ${transaction.category ? `
                                <div class="category-badge d-inline-block">
                                    <span class="badge" style="background-color: ${transaction.category.color || '#6c757d'}; color: white;">
                                        ${transaction.category.name}
                                    </span>
                                </div>
                            ` : ''}
                        </div>
                    </div>

                    <div class="col-12 col-md-6 col-lg-2">
                        <div class="amount ${transaction.amount >= 0 ? 'text-success' : 'text-danger'}">
                            <strong>
                            ${formatCurrency(transaction.amount, transaction.currency.code)}
                            </strong>
                        </div>
                        <small class="text-muted">
                            ${formatDateTime(transaction.operationTime)}
                        </small>
                    </div>

                    <div class="col-12 col-md-9 col-lg-3"  >
                        ${transaction.card ? `
                            <div class="card-info mb-2 card-header">
                                <i class="fas fa-credit-card"></i>
                                <span>**** ${transaction.card.lastFourDigits}</span>
                            </div>
                        ` : ''}
                    </div>



                    <div class="col-12 col-md-3 col-lg-1">
                        <div class="d-flex gap-2 flex-wrap">
                            <button class="btn btn-sm btn-outline-primary" onclick="viewTransactionDetails(${transaction.id})">
                                <i class="fas fa-eye"></i>
                            </button>
                            <button class="btn btn-sm btn-outline-danger" onclick="deleteTransactionById(${transaction.id})">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `).join('');
}

function renderEmptyState() {
    const container = document.getElementById('transactionsContainer');
    container.innerHTML = `
        <div class="col-12">
            <div class="text-center py-5">
                <i class="fas fa-exchange-alt fa-3x text-muted mb-3"></i>
                <h4 class="text-muted">Нет транзакций</h4>
                <p class="text-muted">Добавьте первую транзакцию или измените фильтры</p>
                <button class="btn btn-primary" onclick="openAddTransactionModal()">
                    <i class="fas fa-plus"></i> Добавить транзакцию
                </button>
            </div>
        </div>
    `;
}

function setupEventListeners() {
    // Обработчик формы добавления транзакции
    const addTransactionForm = document.getElementById('addTransactionForm');
    if (addTransactionForm) {
        addTransactionForm.addEventListener('submit', handleAddTransaction);
    }

    // Обработчик формы разделения транзакции
    const splitTransactionForm = document.getElementById('splitTransactionForm');
    if (splitTransactionForm) {
        splitTransactionForm.addEventListener('submit', handleSplitTransaction);
    }

    // Обработчики фильтров
    const filterForm = document.getElementById('filterForm');
    if (filterForm) {
        filterForm.addEventListener('submit', handleFilterSubmit);
    }

    const clearFiltersBtn = document.getElementById('clearFilters');
    if (clearFiltersBtn) {
        clearFiltersBtn.addEventListener('click', clearFilters);
    }
}

async function handleAddTransaction(event) {
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
        await loadTransactions();
    } catch (error) {
        console.error('Failed to create transaction:', error);
        showErrorMessage('Ошибка создания транзакции: ' + error.message);
    }
}

async function handleSplitTransaction(event) {
    event.preventDefault();
    const form = event.target;
    const subTransactions = [];
    let sumAmount = 0;

    // Получаем все подтранзакции из формы
    const items = form.querySelectorAll('.sub-transaction-item');
    items.forEach(item => {
        const description = item.querySelector('input[name*="description"]')?.value;
        const amount = Number.parseFloat(item.querySelector('input[name*="amount"]')?.value);
        if (description && amount) {
            subTransactions.push({ description, amount });
        }
        sumAmount += amount;
    });

    if (subTransactions.length === 0) {
        showErrorMessage('Добавьте хотя бы одну подтранзакцию');
        return;
    }

    const totalAmount = Number.parseFloat(form.dataset.totalAmount);

    if (sumAmount !== totalAmount) {
        if (sumAmount > totalAmount) {
            showErrorMessage('Сумма подтранзакций больше суммы транзакции');
            return;
        }
        const remainingAmount = (totalAmount - sumAmount).toFixed(2);
        showErrorMessage('Общая сумма не совпадает с суммой транзакции, еще нужно ввести ' + remainingAmount);
        return;
    }

    const transactionId = form.dataset.transactionId;

    try {
        await splitTransaction(transactionId, subTransactions);
        showSuccessMessage('Транзакция успешно разделена!');

        if (typeof bootstrap !== 'undefined') {
            const modalElement = document.getElementById('splitTransactionModal');
            const modal = bootstrap.Modal.getInstance(modalElement) || new bootstrap.Modal(modalElement);
            modal.hide();
        }

        await loadTransactions();
    } catch (error) {
        console.error('Failed to split transaction:', error);
        showErrorMessage('Ошибка разделения транзакции: ' + error.message);
    }
}

async function handleFilterSubmit(event) {
    event.preventDefault();
    const form = event.target;
    const formData = new FormData(form);

    currentFilters = {
        startDate: formData.get('startDate') || null,
        endDate: formData.get('endDate') || null,
        cardId: formData.get('cardId') || null,
        bankId: formData.get('bankId') || null,
        categoryId: formData.get('categoryId') || null,
        description: formData.get('description') || null
    };

    await loadTransactions(0); // Reset to first page
}

async function clearFilters() {
    currentFilters = {};
    document.getElementById('filterForm').reset();
    await loadTransactions(0); // Reset to first page
}

function renderPagination() {
    const container = document.getElementById('paginationContainer');
    if (!container) return;

    if (totalPages <= 1) {
        container.innerHTML = '';
        return;
    }

    let paginationHTML = '<nav aria-label="Page navigation"><ul class="pagination justify-content-center">';

    // Previous button
    paginationHTML += `
        <li class="page-item ${currentPage === 0 ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="changePage(${currentPage - 1}); return false;">Предыдущая</a>
        </li>
    `;

    // Page numbers
    const maxVisiblePages = 5;
    let startPage = Math.max(0, currentPage - Math.floor(maxVisiblePages / 2));
    let endPage = Math.min(totalPages - 1, startPage + maxVisiblePages - 1);

    if (endPage - startPage < maxVisiblePages - 1) {
        startPage = Math.max(0, endPage - maxVisiblePages + 1);
    }

    if (startPage > 0) {
        paginationHTML += `<li class="page-item"><a class="page-link" href="#" onclick="changePage(0); return false;">1</a></li>`;
        if (startPage > 1) {
            paginationHTML += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
        }
    }

    for (let i = startPage; i <= endPage; i++) {
        paginationHTML += `
            <li class="page-item ${i === currentPage ? 'active' : ''}">
                <a class="page-link" href="#" onclick="changePage(${i}); return false;">${i + 1}</a>
            </li>
        `;
    }

    if (endPage < totalPages - 1) {
        if (endPage < totalPages - 2) {
            paginationHTML += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
        }
        paginationHTML += `<li class="page-item"><a class="page-link" href="#" onclick="changePage(${totalPages - 1}); return false;">${totalPages}</a></li>`;
    }

    // Next button
    paginationHTML += `
        <li class="page-item ${currentPage >= totalPages - 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="changePage(${currentPage + 1}); return false;">Следующая</a>
        </li>
    `;

    paginationHTML += '</ul></nav>';
    container.innerHTML = paginationHTML;
}

globalThis.changePage = async function (page) {
    if (page < 0 || page >= totalPages) return;
    await loadTransactions(page);
    // Scroll to top of transactions list
    document.getElementById('transactionsContainer')?.scrollIntoView({ behavior: 'smooth' });
};

function formatDateTime(dateTimeString) {
    const date = new Date(dateTimeString);
    return date.toLocaleString('ru-RU', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// Глобальные функции для вызова из HTML
globalThis.openAddTransactionModal = function () {
    if (typeof bootstrap === 'undefined') {
        console.error('Bootstrap is not loaded');
        return;
    }

    populateAddTransactionModal();
    const modal = new bootstrap.Modal(document.getElementById('addTransactionModal'));
    modal.show();
};

globalThis.viewTransactionDetails = async function (transactionId) {
    try {
        const transaction = await fetchTransactionById(transactionId);
        populateTransactionDetailsModal(transaction);

        if (typeof bootstrap !== 'undefined') {
            const modal = new bootstrap.Modal(document.getElementById('transactionDetailsModal'));
            modal.show();
        }
    } catch (error) {
        console.error('Failed to load transaction details:', error);
        showErrorMessage('Ошибка загрузки деталей транзакции: ' + error.message);
    }
};

globalThis.deleteTransactionById = async function (transactionId) {
    if (!confirm('Вы уверены, что хотите удалить эту транзакцию?')) {
        return;
    }

    try {
        await deleteTransaction(transactionId);
        showSuccessMessage('Транзакция успешно удалена!');
        await loadTransactions();
    } catch (error) {
        console.error('Failed to delete transaction:', error);
        showErrorMessage('Ошибка удаления транзакции: ' + error.message);
    }
};

globalThis.toggleHideTransactionById = async function (transactionId, hide) {
    try {
        await toggleHideTransaction(transactionId, hide);
        showSuccessMessage(hide ? 'Транзакция скрыта из статистики' : 'Транзакция показана в статистике');

        // Refresh the modal with updated data
        const transaction = await fetchTransactionById(transactionId);
        populateTransactionDetailsModal(transaction);

        // Reload the transactions list to reflect the change
        await loadTransactions(currentPage);
    } catch (error) {
        console.error('Failed to toggle hide transaction:', error);
        showErrorMessage('Ошибка изменения видимости транзакции: ' + error.message);
    }
};

globalThis.openSplitTransactionModal = function (transactionId, totalAmount) {
    if (typeof bootstrap === 'undefined') {
        console.error('Bootstrap is not loaded');
        return;
    }

    // Закрываем текущее открытое модальное окно
    const existingModal = bootstrap.Modal.getInstance(document.querySelector('.modal.show'));
    if (existingModal) {
        existingModal.hide();
    }

    const form = document.getElementById('splitTransactionForm');
    form.dataset.transactionId = transactionId;
    form.dataset.totalAmount = totalAmount;

    // Очищаем предыдущие подтранзакции
    const container = document.getElementById('subTransactionsContainer');
    container.innerHTML = '';

    const totalAmountElement = document.getElementById('totalAmountDisplay');
    if (totalAmountElement) {
        totalAmountElement.textContent = `${totalAmount}`;
    }

    const modal = new bootstrap.Modal(document.getElementById('splitTransactionModal'));
    modal.show();

    setTimeout(() => {
        updateRemainingAmount();
    }, 100);
};

globalThis.addSubTransaction = function () {
    const container = document.getElementById('subTransactionsContainer');
    const index = container.children.length;


    const totalAmount = Number.parseFloat(document.getElementById('splitTransactionForm').dataset.totalAmount) || 0;
    const amountInputs = document.querySelectorAll('.sub-transaction-amount');

    let totalSubAmount = 0;
    amountInputs.forEach(input => {
        const value = Number.parseFloat(input.value) || 0;
        totalSubAmount += value;
    });

    const remainingAmount = totalAmount - totalSubAmount;

    const subTransactionHtml = `
        <div class="sub-transaction-item mb-3 p-3 border rounded">
            <div class="d-flex justify-content-between align-items-start">
                <div class="flex-grow-1 me-2">
                    <input type="text" class="form-control mb-2" name="subTransactions[${index}].description" placeholder="Описание подтранзакции" required>
                    <input type="number" class="form-control sub-transaction-amount" name="subTransactions[${index}].amount" placeholder="Сумма" step="0.01" value="${remainingAmount > 0 ? remainingAmount.toFixed(2) : ''}" required>
                </div>
                <button type="button" class="btn btn-sm btn-danger" onclick="this.parentElement.parentElement.remove(); updateRemainingAmount();">
                    <i class="fas fa-times"></i>
                </button>
            </div>
        </div>
    `;

    container.insertAdjacentHTML('beforeend', subTransactionHtml);
    const amountInput = container.lastElementChild.querySelector('.sub-transaction-amount');
    amountInput.addEventListener('input', updateRemainingAmount);
    updateRemainingAmount();
};

globalThis.updateRemainingAmount = function () {
    const totalAmount = Number.parseFloat(document.getElementById('splitTransactionForm').dataset.totalAmount) || 0;
    const amountInputs = document.querySelectorAll('.sub-transaction-amount');

    let totalSubAmount = 0;
    amountInputs.forEach(input => {
        const value = Number.parseFloat(input.value) || 0;
        totalSubAmount += value;
    });

    const remainingAmount = totalAmount - totalSubAmount;
    const remainingAmountElement = document.getElementById('remainingAmount');

    if (remainingAmountElement) {
        remainingAmountElement.textContent = remainingAmount.toFixed(2);

        // Меняем цвет в зависимости от остатка
        if (remainingAmount === 0) {
            remainingAmountElement.className = 'mb-3 fw-bold amount text-Success';
        } else if (remainingAmount < 0) {
            remainingAmountElement.className = 'mb-3 fw-bold amount text-danger';
        } else {
            remainingAmountElement.className = 'mb-3 fw-bold amount text-bg-info';
        }
    }
};

function populateTransactionDetailsModal(transaction) {
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
        bankElement.innerHTML = `<span class="badge bg-info">${transaction.bank.name}</span>`;
    } else if (transaction.card) {
        bankElement.textContent = 'Не указан';
    } else {
        bankElement.textContent = 'Не указан';
    }

    // Category with color
    const categoryElement = document.getElementById('detailCategory');
    if (transaction.category) {
        const color = transaction.category.color || '#6c757d';
        categoryElement.innerHTML = `<span class="badge" style="background-color: ${color}; color: white;">${transaction.category.name}</span>`;
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
}

function populateAddTransactionModal() {

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
        option.textContent = `${card.cardName} (****${card.lastFourDigits})`;
        cardSelect.appendChild(option);
    });



    // изменяем цвет суммы в зависимости от типа операции
    document.querySelectorAll('input[type="radio"][name="operationType"]')
        .forEach(radio => {
            radio.addEventListener('change', () => {
                changeAddAmountColor(radio);
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

function getLocalDateTimeString(date = new Date()) {
    // Смещаем дату на разницу с UTC чтобы получить локальное время
    const timezoneOffset = date.getTimezoneOffset() * 60000;
    const localDate = new Date(date.getTime() - timezoneOffset);
    return localDate.toISOString().slice(0, 16);
}


function populateFilters() {
    // Заполнить фильтр категорий
    const categoryFilter = document.getElementById('filterCategory');
    if (categoryFilter) {
        categoriesCache.forEach(category => {
            const option = document.createElement('option');
            option.value = category.id;
            option.textContent = category.name;
            categoryFilter.appendChild(option);
        });
    }

    // Заполнить фильтр карт
    const cardFilter = document.getElementById('filterCard');
    if (cardFilter) {
        cardsCache.forEach(card => {
            const option = document.createElement('option');
            option.value = card.id;
            option.textContent = `${card.cardName} (****${card.lastFourDigits})`;
            cardFilter.appendChild(option);
        });
    }

    // Заполнить фильтр банков
    const bankFilter = document.getElementById('filterBank');
    if (bankFilter) {
        banksCache.forEach(bank => {
            const option = document.createElement('option');
            option.value = bank.id;
            option.textContent = bank.name;
            bankFilter.appendChild(option);
        });
    }
}
