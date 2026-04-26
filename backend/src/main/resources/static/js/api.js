export async function apiFetch(url, options = {}) {
    const response = await fetch(url, {
        credentials: 'same-origin',
        headers: {
            'Content-Type': 'application/json',
            ...(options.headers || {})
        },
        ...options
    });

    if (response.status === 204) {
        return null;
    }

    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
        const error = new Error(data.message || 'Request failed');
        error.status = response.status;
        error.payload = data;
        throw error;
    }
    return data;
}

export const auth = {
    login: (account, password) => apiFetch('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({ account, password })
    }),
    register: (account, password) => apiFetch('/api/auth/register', {
        method: 'POST',
        body: JSON.stringify({ account, password })
    }),
    logout: () => apiFetch('/api/auth/logout', { method: 'POST' })
};

export const blog = {
    getHome: (account) => account ? apiFetch(`/api/users/${account}/home`) : apiFetch('/api/users/me/home'),
    searchUsers: (q) => apiFetch(`/api/users/search?q=${encodeURIComponent(q)}`),
    getNote: (id) => apiFetch(`/api/notes/${id}`),
    updateNote: (id, payload) => apiFetch(`/api/notes/${id}`, {
        method: 'PUT',
        body: JSON.stringify(payload)
    }),
    createFolder: (name) => apiFetch('/api/folders', {
        method: 'POST',
        body: JSON.stringify({ name })
    }),
    createNote: (payload) => apiFetch('/api/notes', {
        method: 'POST',
        body: JSON.stringify(payload)
    })
};
