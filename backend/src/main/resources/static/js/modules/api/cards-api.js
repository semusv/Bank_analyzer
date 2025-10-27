import { handleApiResponse } from "../utils.js";

export async function deleteCard(cardId) {
    const response = await fetch(`/api/card/${cardId}`, {
        method: 'DELETE',
        headers: {
            'Accept': 'application/json'
        }
    });

    return await handleApiResponse(response);
}