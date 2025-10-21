import { handleApiResponse } from "../utils.js";

export async function fetchAccounts() {
    const response = await fetch('/api/accounts', {
        headers: {
            'Accept': 'application/json'
        }
    });

    return await handleApiResponse(response);
}

export async function createAccount(accountData) {
    const response = await fetch('/api/accounts', {
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
    const response = await fetch('/api/cards', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify(cardData)
    });

    return await handleApiResponse(response);
}
