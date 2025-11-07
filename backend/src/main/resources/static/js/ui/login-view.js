document.addEventListener('DOMContentLoaded', () => {

    const form = document.getElementById('loginForm');

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value;

        if (!username || !password) {
            alert('Пожалуйста, заполните все поля');
            return;
        }

        try {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });

            if (response.ok) {
                // Кука JWT_TOKEN установлена сервером
                window.location.href = '/dashboard';  // Редирект работает!
            } else {
                const error = await response.json();
                alert(error.error || 'Ошибка входа');
            }
        } catch (err) {
            alert('Ошибка соединения');
        }
    });
});