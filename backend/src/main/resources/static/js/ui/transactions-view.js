import {
    fetchTransactions,
    fetchTransactionById,
    createTransaction,
    deleteTransaction,
    toggleHideTransaction,
    splitTransaction
} from "../modules/api/transactions-api.js";
import { fetchAccounts } from "../modules/api/accounts-api.js";
import { fetchCards } from "../modules/api/cards-api.js";
import { fetchCategories } from "../modules/api/categories-api.js";
import { fetchBanks } from "../modules/api/banks-api.js";
import { fetchCurrencies } from "../modules/api/currency-api.js";
import { showErrorMessage, showSuccessMessage } from "../modules/utils.js";

document.addEventListener('DOMContentLoaded', init);

let currentFilters = {};
let categoriesCache = null;
let cardsCache = null;
let banksCache = null;
let currenciesCache = null;

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

async function loadTransactions() {
    try {
        const transactions = await fetchTransactions(currentFilters);
        renderTransactions(transactions);
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
        <div class="card transaction-card mb-3 ${transaction.hide ? 'opacity-50' : ''}" data-transaction-id="${transaction.id}">
            <div class="card-body">
                <div class="row align-items-center">
                    <div class="col-12 col-md-6 col-lg-4">
                        <div class="transaction-info">
                            <h6 class="mb-1">
                                ${transaction.description}
                                ${transaction.hide ? '<span class="badge bg-secondary ms-2">Скрыто</span>' : ''}
                            </h6>
                            <small class="text-muted">
                                ${formatDateTime(transaction.operationTime)}
                            </small>
                        </div>
                    </div>
                    <div class="col-12 col-md-6 col-lg-3">
                        ${transaction.card ? `
                            <div class="card-info mb-2">
                                <i class="fas fa-credit-card"></i>
                                <span>**** ${transaction.card.lastFourDigits}</span>
                            </div>
                        ` : ''}
                        ${transaction.category ? `
                            <div class="category-badge d-inline-block">
                                <span class="badge" style="background-color: ${transaction.category.color || '#6c757d'}; color: white;">
                                    ${transaction.category.name}
                                </span>
                            </div>
                        ` : ''}
                    </div>
                    <div class="col-12 col-md-6 col-lg-2">
                        <div class="amount ${transaction.amount >= 0 ? 'text-success' : 'text-danger'}">
                            <strong>${transaction.amount >= 0 ? '+' : ''}${transaction.amount.toLocaleString('ru-RU')} ${transaction.currency.symbol}</strong>
                        </div>
                    </div>
                    <div class="col-12 col-md-6 col-lg-3">
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
        amount: parseFloat(formData.get('amount')),
        operationTime: formData.get('operationTime'),
        currencyId: parseInt(formData.get('currencyId')),
        categoryId: formData.get('categoryId') ? parseInt(formData.get('categoryId')) : null,
        cardId: formData.get('cardId') ? parseInt(formData.get('cardId')) : null
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

    // Получаем все подтранзакции из формы
    const items = form.querySelectorAll('.sub-transaction-item');
    items.forEach(item => {
        const description = item.querySelector('input[name*="description"]')?.value;
        const amount = parseFloat(item.querySelector('input[name*="amount"]')?.value);
        if (description && amount) {
            subTransactions.push({ description, amount });
        }
    });

    if (subTransactions.length === 0) {
        showErrorMessage('Добавьте хотя бы одну подтранзакцию');
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

    await loadTransactions();
}

async function clearFilters() {
    currentFilters = {};
    document.getElementById('filterForm').reset();
    await loadTransactions();
}

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
        await loadTransactions();
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

    const form = document.getElementById('splitTransactionForm');
    form.dataset.transactionId = transactionId;
    form.dataset.totalAmount = totalAmount;

    // Очищаем предыдущие подтранзакции
    const container = document.getElementById('subTransactionsContainer');
    container.innerHTML = '';

    const modal = new bootstrap.Modal(document.getElementById('splitTransactionModal'));
    modal.show();
};

globalThis.addSubTransaction = function () {
    const container = document.getElementById('subTransactionsContainer');
    const index = container.children.length;

    const subTransactionHtml = `
        <div class="sub-transaction-item mb-3 p-3 border rounded">
            <div class="d-flex justify-content-between align-items-start">
                <div class="flex-grow-1 me-2">
                    <input type="text" class="form-control mb-2" name="subTransactions[${index}].description" placeholder="Описание подтранзакции" required>
                    <input type="number" class="form-control" name="subTransactions[${index}].amount" placeholder="Сумма" step="0.01" required>
                </div>
                <button type="button" class="btn btn-sm btn-danger" onclick="this.parentElement.parentElement.remove()">
                    <i class="fas fa-times"></i>
                </button>
            </div>
        </div>
    `;

    container.insertAdjacentHTML('beforeend', subTransactionHtml);
};

function populateTransactionDetailsModal(transaction) {
    document.getElementById('detailDescription').textContent = transaction.description;
    document.getElementById('detailAmount').textContent =
        `${transaction.amount >= 0 ? '+' : ''}${transaction.amount.toLocaleString('ru-RU')} ${transaction.currency.symbol}`;
    document.getElementById('detailAmount').className =
        `amount ${transaction.amount >= 0 ? 'text-success' : 'text-danger'}`;
    document.getElementById('detailDateTime').textContent = formatDateTime(transaction.operationTime);
    document.getElementById('detailCard').textContent = transaction.card ?
        `**** ${transaction.card.lastFourDigits}` : 'Не указана';
    document.getElementById('detailCategory').textContent = transaction.category ?
        transaction.category.name : 'Не указана';
    document.getElementById('detailHide').checked = transaction.hide;

    const hideToggleBtn = document.getElementById('toggleHideBtn');
    hideToggleBtn.onclick = () => toggleHideTransactionById(transaction.id, !transaction.hide);

    const splitBtn = document.getElementById('splitTransactionBtn');
    splitBtn.onclick = () => openSplitTransactionModal(transaction.id, transaction.amount);
}

function populateAddTransactionModal() {
    // Заполнить выбор валют
    const currencySelect = document.getElementById('addCurrencyId');
    currencySelect.innerHTML = '<option value="">Выберите валюту</option>';
    currenciesCache.forEach(currency => {
        const option = document.createElement('option');
        option.value = currency.id;
        option.textContent = `${currency.code} (${currency.name})`;
        currencySelect.appendChild(option);
    });

    // Заполнить выбор категорий
    const categorySelect = document.getElementById('addCategoryId');
    categorySelect.innerHTML = '<option value="">Не выбрано</option>';
    categoriesCache.forEach(category => {
        const option = document.createElement('option');
        option.value = category.id;
        option.textContent = category.name;
        categorySelect.appendChild(option);
    });

    // Заполнить выбор карт
    const cardSelect = document.getElementById('addCardId');
    cardSelect.innerHTML = '<option value="">Не выбрано</option>';
    cardsCache.forEach(card => {
        const option = document.createElement('option');
        option.value = card.id;
        option.textContent = `${card.cardName} (****${card.lastFourDigits})`;
        cardSelect.appendChild(option);
    });
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
