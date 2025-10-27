import { handleApiResponse } from "../utils.js";

export async function fetchCategories() {
    const response = await fetch('/api/category', {
        headers: {
            'Accept': 'application/json'
        }
    });

    return await handleApiResponse(response);
}
