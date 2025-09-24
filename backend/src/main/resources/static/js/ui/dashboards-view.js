import { fetchStats, fetchTransactionsRes } from "../modules/api/dashboards-api.js";
import { showErrorMessage } from "../modules/utils.js";

document.addEventListener('DOMContentLoaded', init);

async function init() {
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

function renderStats(stats) {
    document.getElementById('totalBalance').textContent = stats.totalBalance + ' ₽';
    document.getElementById('monthlyIncome').textContent = stats.monthlyIncome + ' ₽';
    document.getElementById('monthlyExpense').textContent = stats.monthlyExpense + ' ₽';
    document.getElementById('activeBudgets').textContent = stats.activeBudgets;
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
                        <span class="${txn.amount > 0 ? 'transaction-income' : 'transaction-expense'}">
                            ${txn.amount > 0 ? '+' : ''}${txn.amount} ₽
                        </span>
                        <br>
                        <small class="text-muted">${txn.category?.name || 'Без категории'}</small>
                    </div>
                </div>
            `).join('');
}