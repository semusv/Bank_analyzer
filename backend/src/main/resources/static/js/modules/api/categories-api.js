import { handleApiResponse } from "../utils.js";

export async function fetchCategories() {
    const response = await fetch('/api/category', {
        headers: {
            'Accept': 'application/json'
        }
    });

    return await handleApiResponse(response);
}

export async function fetchCategoryById(categoryId) {
    const response = await fetch(`/api/category/${categoryId}`, {
        headers: {
            'Accept': 'application/json'
        }
    });

    return await handleApiResponse(response);
}

export async function createCategory(categoryData) {
    const response = await fetch('/api/category', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify(categoryData)
    });

    return await handleApiResponse(response);
}

export async function updateCategory(categoryId, categoryData) {
    const response = await fetch(`/api/category/${categoryId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify(categoryData)
    });

    return await handleApiResponse(response);
}

export async function deleteCategory(categoryId) {
    const response = await fetch(`/api/category/${categoryId}`, {
        method: 'DELETE',
        headers: {
            'Accept': 'application/json'
        }
    });

    return await handleApiResponse(response);
}

export async function fetchCategoryColors() {
    const response = await fetch('/api/category/colors', {
        headers: {
            'Accept': 'application/json'
        }
    });

    return await handleApiResponse(response);
}
