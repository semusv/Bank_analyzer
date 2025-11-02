import {
    fetchStats,
    fetchTransactionsRes
} from "../modules/api/dashboards-api.js";
import {
    showErrorMessage,
    formatCurrency
} from "../modules/utils.js";
import {
    handleAddTransaction,
    populateAddTransactionModal
} from "./fragments/add-transaction-view.js";
import { fetchCards } from "../modules/api/cards-api.js";
import { fetchCategories } from "../modules/api/categories-api.js";



document.addEventListener('DOMContentLoaded', init);

let categoriesCache = null;
let cardsCache = null;

function init() {
    setupEventListeners();
    loadData();
}

async function loadData() {
    try {
        const [statsRes, transactionsRes] = await Promise.all([
            fetchStats(),
            fetchTransactionsRes(),
        ]);
        renderStats(statsRes);
        renderTransactions(transactionsRes);
    } catch (error) {
        console.error('Failed init page while getting data:', error);
        showErrorMessage(error.message);
    }
}

function setupEventListeners() {
    // Обработчик формы добавления транзакции
    const addTransactionForm = document.getElementById('addTransactionForm');
    if (addTransactionForm) {
        addTransactionForm.addEventListener('submit', handleAddTransaction);
    }
    //Кнопка обновления данных
    const refreshBtn = document.getElementById('refreshBtn');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', loadData);
    }
}

document.addEventListener('transactionAddSuccess', async function (event) {
    await loadData();
});


// Глобальные функции для вызова из HTML
globalThis.openAddTransactionModal = async function () {
    if (typeof bootstrap === 'undefined') {
        console.error('Bootstrap is not loaded');
        showErrorMessage('Ошибка: Bootstrap не загружен. Пожалуйста, обновите страницу.');
        return;
    }

    if (!categoriesCache || !cardsCache) {
        try {
            await Promise.all([
                loadCategories(),
                loadCards()
            ]);
        } catch (error) {
            console.error('Failed to initialize cache:', error);
            showErrorMessage('Ошибка загрузки страницы: ' + error.message);
        }
    }

    populateAddTransactionModal(categoriesCache, cardsCache);
    const modal = new bootstrap.Modal(document.getElementById('addTransactionModal'));
    modal.show();
};

function renderStats(stats) {
    // TODO: Валюты поправить
    document.getElementById('totalBalance').textContent = formatCurrency(stats.totalBalance, 'RUB');
    document.getElementById('monthlyIncome').textContent = formatCurrency(stats.monthlyIncome, 'RUB');
    document.getElementById('monthlyExpense').textContent = formatCurrency(stats.monthlyExpense, 'RUB');
}

async function loadCategories() {
    try {
        categoriesCache = await fetchCategories();
        return categoriesCache;
    } catch (error) {
        console.error('Failed to load categories:', error);
        throw error;
    }
}

async function loadCards() {
    try {
        cardsCache = await fetchCards();
        return cardsCache;
    } catch (error) {
        console.error('Failed to load cards:', error);
        throw error;
    }
}


function renderTransactions(transactions) {
    const container = document.getElementById('recentTransactions');

    if (transactions.length === 0) {
        container.innerHTML = '<p class="text-muted">Нет транзакций</p>';
        return;
    }

    container.innerHTML = transactions.map(txn => `
                <div class="d-flex justify-content-between align-items-center border-bottom py-2">
                    <div>
                        <strong>${txn.description}</strong>
                        <br>
                        <small class="text-muted">${new Date(txn.operationTime).toLocaleDateString()}</small>
                    </div>
                    <div class="text-end">
                        <span class="amount ${txn.amount > 0 ? 'text-success' : 'text-danger'}">
                            <strong>
                                ${formatCurrency(txn.amount, txn.currency.code)}
                            </strong>
                        </span>
                        <br>
                        <small class="text-muted">${txn.category?.name || 'Без категории'}</small>
                    </div>
                </div>
            `).join('');
}