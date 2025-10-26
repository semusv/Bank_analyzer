import { handleApiResponse } from "../utils.js";

export async function fetchBanks() {

    const response = await fetch(`/api/bank`, {
        headers: {
            'Accept': 'application/json'
        }
    });

    return await handleApiResponse(response);
}