import {
    fetchLogin
} from "../modules/api/auth-api.js";

document.addEventListener('DOMContentLoaded', () => {

    const form = document.getElementById('loginForm');

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value;

        try {
            await fetchLogin(username, password);
            globalThis.location.href = '/';
        } catch (error) {
            console.error('Failed to login:', error);
            globalThis.location.href = '/login?error';
        }
    });
});