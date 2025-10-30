import { handleApiResponse } from "../utils.js";




export async function fetchTransactions(filters = {}) {
    const params = new URLSearchParams();

    if (filters.startDate) params.append('startDate', filters.startDate);
    if (filters.endDate) params.append('endDate', filters.endDate);
    if (filters.cardId) params.append('cardId', filters.cardId);
    if (filters.bankId) params.append('bankId', filters.bankId);
    if (filters.categoryId) params.append('categoryId', filters.categoryId);
    if (filters.description) params.append('description', filters.description);

    // Pagination parameters
    const page = filters.page || 0;
    const size = filters.size || 20;
    params.append('page', page);
    params.append('size', size);

    const queryString = params.toString();
    const url = `/api/transaction?${queryString}`;

    const response = await fetch(url, {
        headers: {
            'Accept': 'application/json'
        }
    });

    return await handleApiResponse(response);
}

export async function fetchTransactionById(transactionId) {
    const response = await fetch(`/api/transaction/${transactionId}`, {
        headers: {
            'Accept': 'application/json'
        }
    });

    return await handleApiResponse(response);
}

export async function createTransaction(transactionData) {
    const response = await fetch('/api/transaction', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify(transactionData)
    });

    return await handleApiResponse(response);
}

export async function updateTransaction(transactionId, transactionData) {
    const response = await fetch(`/api/transaction/${transactionId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify(transactionData)
    });

    return await handleApiResponse(response);
}

export async function deleteTransaction(transactionId) {
    const response = await fetch(`/api/transaction/${transactionId}`, {
        method: 'DELETE',
        headers: {
            'Accept': 'application/json'
        }
    });

    return await handleApiResponse(response);
}

export async function toggleHideTransaction(transactionId, hide) {
    const response = await fetch(`/api/transaction/${transactionId}/hide`, {
        method: 'PATCH',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify({ hide })
    });

    return await handleApiResponse(response);
}

export async function splitTransaction(transactionId, subTransactions) {
    const response = await fetch(`/api/transaction/${transactionId}/split`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify(subTransactions)
    });

    return await handleApiResponse(response);
}
