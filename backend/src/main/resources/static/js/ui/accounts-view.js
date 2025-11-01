import { fetchAccounts, createAccount, createCard, deleteAccount as deleteAccountApi } from "../modules/api/accounts-api.js";
import { deleteCard as deleteCardApi } from "../modules/api/cards-api.js";
import { fetchBanks } from "../modules/api/banks-api.js";
import { showErrorMessage, showSuccessMessage, formatCurrency, showApiErrors } from "../modules/utils.js";
import { fetchCurrencies } from "../modules/api/currency-api.js";
import { getBankColorsForElem } from "./themes.js";

document.addEventListener('DOMContentLoaded', init);
let banksCache = null;
let currenciesCache = null;


async function init() {
    try {
        await loadBanks();
        await loadAccounts();
        await loadCurrencies();
        setupEventListeners();
    } catch (error) {
        console.error('Failed to initialize accounts page:', error);
        showApiErrors(error);
    }
}

async function loadCurrencies() {
    if (currenciesCache) {
        return currenciesCache;
    }

    try {
        currenciesCache = await fetchCurrencies();
        renderCurrencies(currenciesCache);
        return currenciesCache;
    } catch (error) {
        console.error('Failed to load currencies:', error);
        throw error;
    }
}

async function loadBanks() {
    if (banksCache) {
        return banksCache;
    }

    try {
        banksCache = await fetchBanks();
        renderBankList(banksCache);
        return banksCache;
    } catch (error) {
        console.error('Failed to load banks:', error);
        throw error;
    }
}

async function loadAccounts() {
    try {
        const accounts = await fetchAccounts();
        renderAccounts(accounts);
    } catch (error) {
        console.error('Failed to load accounts:', error);
        renderEmptyState();
        throw error;
    }
}

function renderCurrencies(currencies) {

    const currencySelect = document.getElementById('currency-list');
    currencySelect.innerHTML = '';

    const placeholderOption = document.createElement('option');
    placeholderOption.value = "";
    placeholderOption.textContent = "Выберите валюту";
    placeholderOption.disabled = true;
    placeholderOption.selected = true;
    currencySelect.appendChild(placeholderOption);
    for (const currency of currencies) {
        const option = document.createElement('option');
        option.value = currency.id;
        option.textContent = `${currency.code} (${currency.name})`;
        currencySelect.appendChild(option);
    }
}


function renderBankList(banks) {
    const bankList = document.getElementById('bank-list');
    bankList.innerHTML = '';

    const placeholderOption = document.createElement('option');
    placeholderOption.value = "";
    placeholderOption.textContent = "Выберите банк";
    placeholderOption.disabled = true;
    placeholderOption.selected = true;
    bankList.appendChild(placeholderOption);

    for (const bank of banks) {
        const option = document.createElement('option');
        option.value = bank.id;
        option.textContent = bank.name;
        bankList.appendChild(option);
    }

}

function renderAccounts(accounts) {
    const container = document.getElementById('accountsContainer');

    if (accounts.length === 0) {
        renderEmptyState();
        return;
    }

    container.innerHTML = accounts.map(account => `
        <div class="col-md-6 col-lg-4 mb-4">
            <div class="card account-card h-100" data-bank="${account.bankName}" data-accountId="${account.id}" fa>
                <div class="card-header d-flex justify-content-between align-items-center">
                    <h6 class="mb-0">
                        <i class="fas fa-university"></i> ${account.bankName}
                    </h6>
                    <span class="badge bg-primary">${account.currencyCode}</span>
                </div>

                <div class="card-body">
                    <h5 class="card-title">${account.name}</h5>
                    <p class="card-text text-muted small">
                        <i class="fas fa-hashtag"></i> ${account.accountNumber}
                    </p>
                    <div class="balance-section">
                        <div class="balance-amount ${account.balance >= 0 ? 'text-success' : 'text-danger'}">
                            ${formatCurrency(account.balance, account.currencyCode)}
                        </div>
                    </div>

                    ${account.cards.length > 0 ? `
                        <div class="cards-section mt-3">
                            <h6 class="cards-title">
                                <i class="fas fa-credit-card"></i> Карты (${account.cards.length})
                            </h6>
                            <div class="cards-list">
                                ${account.cards.map(card => `
                                    <div class="card-item">
                                        <div class="card-info d-flex justify-content-between align-items-center">
                                            <div class="card-info-left">
                                                <span class="card-name" title="${card.cardName}">
                                                    ${card.cardName}
                                                </span>
                                                <span class="card-number">**** ${card.lastFourDigits}</span>
                                            </div>
                                            <button class="btn btn-sm btn-icon-delete"
                                                    onclick="deleteCard(${card.id}, ${account.id})"
                                                    title="Удалить карту">
                                                <i class="fas fa-times"></i>
                                            </button>
                                        </div>
                                    </div>
                                `).join('')}
                            </div>
                        </div>
                    ` : `
                        <div class="no-cards mt-3">
                            <button class="btn btn-sm btn-outline-primary" onclick="openAddCardModal(${account.id})">
                                <i class="fas fa-plus"></i> Добавить карту
                            </button>
                        </div>
                    `}
                </div>


                <div class="card-footer">
                    <div class="d-flex gap-2 justify-content-between flex-wrap">
                        <button class="btn btn-sm btn-outline-secondary flex-fill" onclick="viewAccountDetails(${account.id})">
                            <i class="fas fa-eye"></i> <span class="btn-text">Подробно</span>
                        </button>

                        <button class="btn btn-sm btn-outline-primary flex-fill" onclick="openAddCardModal(${account.id})">
                            <i class="fas fa-credit-card"></i> <span class="btn-text">Карта</span>
                        </button>

                        <button class="btn btn-sm btn-outline-danger flex-fill" onclick="deleteAccount(${account.id})">
                            <i class="fas fa-trash"></i> <span class="btn-text">Удалить</span>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    `).join('');

    applyBankThemesToAccounts(accounts);
};



async function applyBankThemesToAccounts(accounts) {
    if (!accounts || accounts.length === 0) return;

    const themePromises = accounts.map(async (txn) => {
        // const { bank: { bankCode }, id } = txn;
        const cardElement = document.querySelector(
            `.account-card[data-accountId="${txn.id}"] .card-header`
        );

        if (cardElement) {
            await getBankColorsForElem(txn.bankCode, cardElement);
        }
    });

    try {
        await Promise.all(themePromises);
    } catch (error) {
        console.error('Failed to apply bank themes:', error);
    }
}
function renderEmptyState() {
    const container = document.getElementById('accountsContainer');
    container.innerHTML = `
        <div class="col-12">
            <div class="text-center py-5">
                <i class="fas fa-credit-card fa-3x text-muted mb-3"></i>
                <h4 class="text-muted">Нет счетов</h4>
                <p class="text-muted">Добавьте свой первый счет для начала работы</p>
                <button class="btn btn-primary" onclick="openAddAccountModal()">
                    <i class="fas fa-plus"></i> Добавить счет
                </button>
            </div>
        </div>
    `;
};

function setupEventListeners() {
    // Обработчик формы добавления счета
    const addAccountForm = document.getElementById('addAccountForm');
    if (addAccountForm) {
        addAccountForm.addEventListener('submit', handleAddAccount);
    }

    // Обработчик формы добавления карты
    const addCardForm = document.getElementById('addCardForm');
    if (addCardForm) {
        addCardForm.addEventListener('submit', handleAddCard);
    }
};

async function handleAddAccount(event) {
    event.preventDefault();
    const form = event.target;
    const formData = new FormData(form);

    if (!form.checkValidity()) {
        form.reportValidity();
        return;
    }

    const accountData = {
        name: formData.get('accountName') || document.getElementById('accountName').value,
        accountNumber: formData.get('accountNumber') || document.getElementById('accountNumber').value,
        initialBalance: Number.parseFloat(formData.get('initialBalance') || document.getElementById('initialBalance').value) || 0,
        bankId: formData.get('bankId'),
        currencyId: formData.get('currencyId'),
    };

    try {
        await createAccount(accountData);
        showSuccessMessage('Счет успешно добавлен!');

        // Закрыть модальное окно
        if (typeof bootstrap !== 'undefined') {
            const modalElement = document.getElementById('addAccountModal');
            const modal = bootstrap.Modal.getInstance(modalElement) || new bootstrap.Modal(modalElement);
            modal.hide();
        }

        // Очистить форму
        event.target.reset();

        // Перезагрузить список счетов
        await loadAccounts();
    } catch (error) {
        console.error('Failed to create account:', error);
        showApiErrors(error);
    }
};

async function handleAddCard(event) {
    event.preventDefault();
    const form = event.target;
    const formData = new FormData(form);

    if (!form.checkValidity()) {
        form.reportValidity();
        return;
    }

    const cardData = {
        accountId: Number.parseFloat(document.getElementById('cardAccountId').value),
        cardName: formData.get('cardName') || document.getElementById('cardName').value,
        lastFourDigits: formData.get('lastFourDigits') || document.getElementById('lastFourDigits').value
    };

    try {
        await createCard(cardData);
        showSuccessMessage('Карта успешно добавлена!');

        // Закрыть модальное окно
        if (typeof bootstrap !== 'undefined') {
            const modalElement = document.getElementById('addCardModal');
            const modal = bootstrap.Modal.getInstance(modalElement) || new bootstrap.Modal(modalElement);
            modal.hide();
        }

        // Очистить форму
        event.target.reset();

        // Перезагрузить список счетов
        await loadAccounts();
    } catch (error) {
        console.error('Failed to create card:', error);
        showApiErrors(error);
    }
};

// Глобальные функции для вызова из HTML
globalThis.openAddAccountModal = function () {
    if (typeof bootstrap === 'undefined') {
        console.error('Bootstrap is not loaded');
        showErrorMessage('Bootstrap не загружен. Проверьте подключение скриптов.');
        return;
    }
    const modal = new bootstrap.Modal(document.getElementById('addAccountModal'));
    modal.show();
};

globalThis.openAddCardModal = function (accountId) {
    if (typeof bootstrap === 'undefined') {
        console.error('Bootstrap is not loaded');
        showErrorMessage('Bootstrap не загружен. Проверьте подключение скриптов.');
        return;
    }
    document.getElementById('cardAccountId').value = accountId;
    const modal = new bootstrap.Modal(document.getElementById('addCardModal'));
    modal.show();
};

globalThis.viewAccountDetails = function (accountId) {
    // TODO: Реализовать просмотр деталей счета
    console.log('View account details:', accountId);
    showSuccessMessage('Функция просмотра деталей счета будет реализована позже');
};

globalThis.deleteAccount = async function (accountId) {
    if (!confirm('Вы уверены, что хотите удалить этот счет и все карты?')) {
        return;
    }

    try {
        await deleteAccountApi(accountId);
        showSuccessMessage('Счет успешно удален!');
        await loadAccounts();
    } catch (error) {
        console.error('Failed to delete account:', error);
        showApiErrors(error);
    }
    await loadAccounts();
};

globalThis.deleteCard = async function (cardId, accountId) {
    if (!confirm('Вы уверены, что хотите удалить эту карту?')) {
        return;
    }

    try {
        await deleteCardApi(cardId);
        showSuccessMessage('Карта успешно удалена!');
        await loadAccounts();
    } catch (error) {
        console.error('Failed to delete card:', error);
        showErrorMessage('Ошибка удаления карты: ' + error.message);
    }
    await loadAccounts();
};
