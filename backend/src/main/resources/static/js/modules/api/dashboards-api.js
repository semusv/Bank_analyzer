import { handleApiResponse } from "../utils.js";

export async function fetchStats() {

    const response = await fetch(`/api/dashboard/stats`, {
        headers: {
            'Accept': 'application/json'
        }
    });

    return await handleApiResponse(response);
}

export async function fetchTransactionsRes() {

    const response = await fetch('/api/dashboard/stats', {
        headers: {
            'Accept': '/api/transactions?limit=5'
        }
    });

    return await handleApiResponse(response);
}
