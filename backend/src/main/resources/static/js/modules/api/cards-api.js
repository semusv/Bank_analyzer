import { handleApiResponse } from "../utils.js";

export async function fetchCards() {
    const response = await fetch('/api/card', {
        headers: {
            'Accept': 'application/json'
        }
    });

    return await handleApiResponse(response);
}

export async function fetchCardById(cardId) {
    const response = await fetch(`/api/card/${cardId}`, {
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