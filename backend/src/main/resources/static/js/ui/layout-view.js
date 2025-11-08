import {
    fetchLogout
} from "../modules/api/auth-api.js";
import {
    showApiErrors
} from "../modules/utils.js";

document.addEventListener('DOMContentLoaded', () => {
    const logoutButton = document.getElementById('logout-btn');
    if (logoutButton) {
        logoutButton.addEventListener('click', logout);
    }
});

export async function logout() {
    localStorage.removeItem('JWT_TOKEN');
    localStorage.removeItem('userName');

    try {
        await fetchLogout();
        globalThis.location.href = '/login?logout';
    } catch (error) {
        console.error('Failed to logout:', error);
        showApiErrors(error);
    }
}
