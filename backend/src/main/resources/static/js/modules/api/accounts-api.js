import { handleApiResponse } from "../utils.js";

export async function fetchAccounts() {
    const response = await fetch('/api/bankAccount', {
        headers: {
            'Accept': 'application/json'
        }
    });

    return await handleApiResponse(response);
}

export async function createAccount(accountData) {
    const response = await fetch('/api/bankAccount', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify(accountData)
    });

    return await handleApiResponse(response);
}

export async function createCard(cardData) {
    const response = await fetch('/api/card', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify(cardData)
    });

    return await handleApiResponse(response);
}

export async function deleteAccount(accountId) {
    const response = await fetch(`/api/bankAccount/${accountId}`, {
        method: 'DELETE',
        headers: {
            'Accept': 'application/json'
        }
    });

    return await handleApiResponse(response);
}

export async function deleteCard(cardId) {
    const response = await fetch(`/api/card/${cardId}`, {
        method: 'DELETE',
        headers: {
            'Accept': 'application/json'
        }
    });

    return await handleApiResponse(response);
}
