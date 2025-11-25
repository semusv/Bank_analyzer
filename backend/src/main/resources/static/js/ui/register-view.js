import {
    fetchRegister
} from "../modules/api/auth-api.js";
import {
    showApiErrors
} from "../modules/utils.js";
document.addEventListener('DOMContentLoaded', () => {

    const form = document.getElementById('registerForm');

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        const form = event.target;
        const formData = new FormData(form);
        form.checkValidity();


        const registerData = {
            login: formData.get('login'),
            password: formData.get('password'),
            name: formData.get('name'),
            surname: formData.get('surname'),
            patronymic: formData.get('patronymic'),
            email: formData.get('email'),
        };

        try {
            await fetchRegister(registerData);
            globalThis.location.href = '/login';
        } catch (error) {
            console.error('Failed to register:', error);
            showApiErrors(error);
        }
    });
});