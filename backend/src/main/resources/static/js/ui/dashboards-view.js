import {
    fetchStats,
    fetchTransactionsRes
} from "../modules/api/dashboards-api.js";
import {
    fetchTransactionById
} from "../modules/api/transactions-api.js";
import {
    showErrorMessage,
    formatCurrency,
    formatDateTime
} from "../modules/utils.js";
import {
    handleAddTransaction,
    populateAddTransactionModal
} from "./fragments/add-transaction-view.js";
import { fetchCards } from "../modules/api/cards-api.js";
import { fetchCategories } from "../modules/api/categories-api.js";
import {
    populateTransactionDetailsModal
} from "./fragments/show-transaction-view.js";


document.addEventListener('DOMContentLoaded', init);

let categoriesCache = null;
let cardsCache = null;

function init() {
    setupEventListeners();
    initTooltips()
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

function initTooltips() {
    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
    const tooltipList = [...tooltipTriggerList].map(
        tooltipTrigger => new bootstrap.Tooltip(tooltipTrigger, {
            html: true,
            placement: 'top'
        })
    );
    return tooltipList;
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
    document.getElementById('totalBalance').textContent = formatCurrency(stats.totalBalanceRub, 'RUB');
    document.getElementById('monthlyIncome').textContent = formatCurrency(stats.monthlyIncomeRub, 'RUB');
    document.getElementById('monthlyExpense').textContent = formatCurrency(stats.monthlyExpenseRub, 'RUB');

    document.getElementById('totalBalance').setAttribute('title', createTooltipHtml(stats.totalBalances));
    document.getElementById('monthlyIncome').setAttribute('title', createTooltipHtml(stats.monthlyIncomes));
    document.getElementById('monthlyExpense').setAttribute('title', createTooltipHtml(stats.monthlyExpenses));

    initTooltips();
}
function createTooltipHtml(currencyAmounts) {
    if (!currencyAmounts || currencyAmounts.length === 0) {
        return 'Нет данных';
    }

    return currencyAmounts
        .map(item => {
            const amount = formatCurrency(item.amount, item.currencyCode);
            return `<div>${item.currencyCode}: ${amount}</div>`;
        })
        .join('');
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

    container.innerHTML = transactions.map(thx => `
                    <div class="transaction-card row align-items-center border-bottom ${thx.hide ? 'opacity-50' : ''}"
                            onclick="viewTransactionDetails(${thx.id})">
                        <div class="col-12 col-md-8 col-lg-8 mb-1">
                            <div class="transaction-info">
                                <h6 class="mb-1">
                                        ${thx.description}
                                    ${thx.hide ? '<span class="badge bg-secondary ms-2">Скрыто</span>' : ''}
                                </h6>
                                ${thx.category ? `
                                    <div class="category-badge d-inline-block">
                                        <span class="badge" style="background-color: ${thx.category.color || '#6c757d'}; color: white;">
                                            ${thx.category.name}
                                        </span>
                                    </div>
                                ` : ''}
                            </div>
                        </div>
    
                        <div class="col-12 col-md-4 col-lg-4 text-end">
                            <div class="amount ${thx.amount >= 0 ? 'text-success' : 'text-danger'} ">
                                <strong>
                                ${formatCurrency(thx.amount, thx.currency.code)}
                                </strong>
                            </div>
                            <small class="text-muted text-end">
                                ${formatDateTime(thx.operationTime)}
                            </small>
                        </div>
                    </div>
        `).join('');
}

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
        showApiErrors(error);
    }
};