/**
 * Módulo centralizado para peticiones HTTP a la API.
 */

const API_BASE = '';

export function escapeHtml(value) {
    const replacements = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;'
    };
    return String(value ?? '').replace(/[&<>"']/g, character => replacements[character]);
}

export function bindTableActions(container, handlers) {
    container.addEventListener('click', event => {
        const button = event.target.closest('button[data-action][data-id]');
        if (!button || !container.contains(button)) return;

        const id = Number(button.dataset.id);
        if (!Number.isSafeInteger(id) || id <= 0) return;

        const action = button.dataset.action;
        if (Object.prototype.hasOwnProperty.call(handlers, action)) handlers[action](id);
    });
}

async function request(path, options = {}) {
    const url = `${API_BASE}${path}`;
    const config = {
        headers: {
            'Accept': 'application/json',
            ...options.headers,
        },
        ...options,
    };

    if (config.body && !(config.body instanceof FormData)) {
        config.headers['Content-Type'] = 'application/json; charset=UTF-8';
        config.body = JSON.stringify(config.body);
    }

    const response = await fetch(url, config);

    if (!response.ok) {
        if (response.status === 204) return null;
        const error = await response.json().catch(() => ({ mensaje: 'Error desconocido' }));
        throw new Error(error.mensaje || `Error ${response.status}: ${response.statusText}`);
    }

    if (response.status === 204) return null;
    return response.json();
}

export async function get(path) {
    return request(path, { method: 'GET' });
}

export async function post(path, body) {
    return request(path, { method: 'POST', body });
}

export async function put(path, body) {
    return request(path, { method: 'PUT', body });
}

export async function del(path) {
    return request(path, { method: 'DELETE' });
}
