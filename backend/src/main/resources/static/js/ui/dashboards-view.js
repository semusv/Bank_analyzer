import { fetchStats, fetchTransactionsRes } from "../modules/api/dashboards-api.js";
import { showErrorMessage, getCurrencyFormatter, formatCurrency } from "../modules/utils.js";

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
    const formatter = getCurrencyFormatter('RUB');

    document.getElementById('totalBalance').textContent = formatCurrency(stats.totalBalance, 'RUB');
    document.getElementById('monthlyIncome').textContent = formatCurrency(stats.monthlyIncome, 'RUB');
    document.getElementById('monthlyExpense').textContent = formatCurrency(stats.monthlyExpense, 'RUB');
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