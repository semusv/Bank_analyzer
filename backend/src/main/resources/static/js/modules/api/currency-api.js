import { handleApiResponse } from "../utils.js";

export async function fetchCurrencies() {

    const response = await fetch(`/api/currency`, {
        headers: {
            'Accept': 'application/json'
        }
    });

    return await handleApiResponse(response);
}