/**
 * API Service - Handles all communications with the backend
 */
const API = {
    baseUrl: '/api',

    async request(endpoint, options = {}) {
        const url = `${this.baseUrl}${endpoint}`;
        const defaultOptions = {
            headers: {
                'Content-Type': 'application/json',
            },
        };

        // If body is FormData, don't set Content-Type header manually
        if (options.body instanceof FormData) {
            delete defaultOptions.headers['Content-Type'];
        }

        const mergedOptions = { ...defaultOptions, ...options };
        
        try {
            const response = await fetch(url, mergedOptions);
            
            if (response.status === 204) return null;
            
            const data = await response.json();
            
            if (!response.ok) {
                throw new Error(data.message || '请求失败');
            }
            
            return data;
        } catch (error) {
            console.error(`API Error [${endpoint}]:`, error);
            throw error;
        }
    },

    // Auth
    login(account, password) {
        return this.request('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ account, password })
        });
    },

    register(account, password) {
        return this.request('/auth/register', {
            method: 'POST',
            body: JSON.stringify({ account, password })
        });
    },

    logout() {
        return this.request('/auth/logout', { method: 'POST' });
    },

    // User & Home
    getMeHome() {
        return this.request('/users/me/home');
    },

    getUserHome(account) {
        return this.request(`/users/${account}/home`);
    },

    searchUsers(query) {
        return this.request(`/users/search?q=${encodeURIComponent(query)}`);
    },

    // Folders
    createFolder(name) {
        return this.request('/folders', {
            method: 'POST',
            body: JSON.stringify({ name })
        });
    },

    deleteFolder(folderId) {
        return this.request(`/folders/${folderId}`, {
            method: 'DELETE'
        });
    },

    // Notes
    getNote(noteId) {
        return this.request(`/notes/${noteId}`);
    },

    createNote(formData) {
        return this.request('/notes', {
            method: 'POST',
            body: formData
        });
    },

    updateNote(noteId, formData) {
        return this.request(`/notes/${noteId}`, {
            method: 'PUT',
            body: formData
        });
    },

    deleteNote(noteId) {
        return this.request(`/notes/${noteId}`, {
            method: 'DELETE'
        });
    }
};
