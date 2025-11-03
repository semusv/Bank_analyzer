export class ApiError extends Error {
    constructor(message, type, status, errors = []) {
        super(message);
        this.name = 'ApiError';
        this.type = type;
        this.status = status;
        this.errors = errors;
    }
}
export async function handleApiResponse(response) {
    if (response.ok) {
        if (response.status === 204) {
            return null;
        }
        return await response.json();
    }

    const errorData = await response.json();

    // Валидационные ошибки
    if (errorData.status === 400) {
        throw new ApiError(
            errorData.message || errorData.errorText || 'Validation failed',
            errorData.errorType,
            response.status,
            errorData.errors
        );
    }

    // Стандартные API ошибки
    throw new ApiError(
        errorData.message || errorData.errorText || `HTTP error ${response.status}`,
        errorData.errorType || 'API_ERROR',
        response.status,
        errorData.errors
    );
}

function showNotification(message, type = 'success') {
    const container = document.getElementById('notification-container');
    const notification = document.createElement('div');
    notification.className = `alert alert-${type} notification`;

    notification.innerHTML = `
            ${message}
            <button class="close" onclick="this.parentElement.remove()">&times;</button>
        `;

    container.appendChild(notification);

    // Автоматическое удаление через 5 секунд
    setTimeout(() => {
        if (notification.parentNode) {
            notification.remove();
        }
    }, 5000);
}

// Примеры использования:
export function showSuccessMessage(message) {
    showNotification(message, 'success');
}

export function showErrorMessage(message) {
    showNotification(message, 'danger');
}

export function showWarningMessage(message) {
    showNotification(message, 'warning');
}

export function delayLoader(minTime, startTime) {
    const elapsedTime = Date.now() - startTime;
    const remainingTime = minTime - elapsedTime;

    if (remainingTime > 0) {
        return new Promise(resolve => setTimeout(resolve, remainingTime));
    }
    return Promise.resolve();
}

export function getLocaleMessage(messageKey, ...args) {
    try {
        const messages = document.getElementById('i18n-messages').dataset;
        let message = messages[messageKey];

        if (message === undefined) {
            console.warn(`Message key "${messageKey}" not found`);
            return messageKey;
        }

        // Заменяем подстановки {0}, {1} и т.д. на соответствующие аргументы
        args.forEach((arg, index) => {
            const placeholder = `{${index}}`;
            message = message.replaceAll(new RegExp(escapeRegExp(placeholder), 'g'), arg);
        });

        return message;
    } catch (error) {
        console.error('Error parsing messages:', error);
        return messageKey;
    }
}
function escapeRegExp(string) {
    return string.replaceAll(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

export function getCurrencyFormatter(currency) {
    const locale = document.querySelector('meta[name="locale"]').content;
    const formatter = new Intl.NumberFormat(locale,
        {
            style: 'currency',
            currency: currency,
            minimumFractionDigits: 0,
            maximumFractionDigits: 0
        });
    return formatter;
}

export function formatCurrency(value, currency) {
    try {
        const formatter = getCurrencyFormatter(currency);
        return formatter.format(value);
    } catch (error) {
        console.error('Error formatting currency:', error);
        return value;
    }
}

export function showApiErrors(error) {
    console.error('API Error:', error);

    if (error.status === 400 && Array.isArray(error.errors) && error.errors.length > 0) {
        error.errors.forEach(errorDet => {
            showErrorMessage(errorDet.field + ' ' + errorDet.message);
        });
    } else {
        showErrorMessage(error.message);
    }
}

export function getLocalDateTimeString(date = new Date()) {
    // Смещаем дату на разницу с UTC чтобы получить локальное время
    const timezoneOffset = date.getTimezoneOffset() * 60000;
    const localDate = new Date(date.getTime() + timezoneOffset);
    return localDate.toISOString().slice(0, 16);
}

export function formatDateTime(dateTimeString) {
    const date = new Date(dateTimeString);
    return date.toLocaleString('ru-RU', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}