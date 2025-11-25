import { handleApiResponse } from "../utils.js";

export async function fetchLogin(username, password) {

    const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
    });


    return await handleApiResponse(response);
}


export async function fetchLogout() {
    const response = await fetch('/api/auth/logout', {
        method: 'POST',
        credentials: 'include', // Обязательно для отправки кук
        headers: {
            'Content-Type': 'application/json',
        }
    });

    return await handleApiResponse(response);
}


export async function fetchRegister(registerData) {
    const response = await fetch('/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(registerData)
    });
    return await handleApiResponse(response);
}