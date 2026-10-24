const API_BASE = '/api';

class ApiClient {
    constructor() {
        this.baseUrl = API_BASE;
    }

    getToken() {
        return localStorage.getItem('token');
    }

    async request(endpoint, options = {}) {
        const url = `${this.baseUrl}${endpoint}`;
        const token = this.getToken();

        const config = {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers,
            },
            ...options,
        };

        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }

        // Don't set Content-Type for FormData
        if (options.body instanceof FormData) {
            delete config.headers['Content-Type'];
        }

        try {
            const response = await fetch(url, config);

            if (response.status === 401) {
                localStorage.removeItem('token');
                localStorage.removeItem('user');
                window.location.href = '/login.html';
                return;
            }

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.error || `Request failed with status ${response.status}`);
            }

            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                return await response.json();
            }
            return await response.text();
        } catch (error) {
            console.error('API Error:', error);
            throw error;
        }
    }

    get(endpoint) {
        return this.request(endpoint, { method: 'GET' });
    }

    post(endpoint, data) {
        return this.request(endpoint, {
            method: 'POST',
            body: JSON.stringify(data),
        });
    }

    put(endpoint, data) {
        return this.request(endpoint, {
            method: 'PUT',
            body: JSON.stringify(data),
        });
    }

    delete(endpoint) {
        return this.request(endpoint, { method: 'DELETE' });
    }

    async uploadFiles(endpoint, files) {
        const formData = new FormData();
        for (let i = 0; i < files.length; i++) {
            formData.append('files', files[i]);
        }
        return this.request(endpoint, {
            method: 'POST',
            body: formData,
        });
    }

    // Auth
    register(data) { return this.post('/auth/register', data); }
    login(data) { return this.post('/auth/login', data); }

    // Items
    getItems(params = {}) {
        const query = new URLSearchParams(params).toString();
        return this.get(`/items?${query}`);
    }
    getItem(id) { return this.get(`/items/${id}`); }
    createItem(data) { return this.post('/items', data); }
    deleteItem(id) { return this.delete(`/items/${id}`); }
    updateItemListing(id, listed) {
        return this.put(`/items/${id}/listing?listed=${listed}`);
    }
    getMyItems(type) { return this.get(`/items/my${type ? `?type=${type}` : ''}`); }
    getPossibleMatches(id) { return this.get(`/items/${id}/matches`); }

    // Images
    uploadImages(itemId, files) { return this.uploadFiles(`/images/item/${itemId}`, files); }
    getItemImages(itemId) { return this.get(`/images/item/${itemId}`); }
    deleteImage(imageId) { return this.delete(`/images/${imageId}`); }

    // Claims
    createClaim(data) { return this.post('/claims', data); }
    getItemClaims(itemId) { return this.get(`/claims/item/${itemId}`); }
    getMyClaims() { return this.get('/claims/my'); }
    updateClaimStatus(claimId, data) { return this.put(`/claims/${claimId}/status`, data); }

    // Conversations
    getConversations() { return this.get('/conversations'); }
    getMessages(conversationId, page = 0) { return this.get(`/conversations/${conversationId}/messages?page=${page}`); }
    sendMessage(conversationId, data) { return this.post(`/conversations/${conversationId}/messages`, data); }
    sendVerificationQuestion(conversationId, question) {
        return this.post(`/conversations/${conversationId}/verification-question`, { question });
    }

    // Notifications
    getNotifications(page = 0) { return this.get(`/notifications?page=${page}`); }
    getUnreadCount() { return this.get('/notifications/unread-count'); }
    markAllNotificationsRead() { return this.put('/notifications/mark-all-read'); }

    // Handovers
    createHandover(data) { return this.post('/handovers', data); }
    getMyHandovers() { return this.get('/handovers/my'); }
    confirmHandover(id) { return this.put(`/handovers/${id}/confirm`); }

    // Reports
    createReport(data) { return this.post('/reports', data); }

    // Profile
    getProfile() { return this.get('/profile'); }
    updateProfile(data) { return this.put('/profile', data); }

    // Locations
    getLocations() { return this.get('/locations'); }

    // Admin
    getAdminDashboard() { return this.get('/admin/dashboard'); }
    getAdminUsers() { return this.get('/admin/users'); }
    toggleUserStatus(userId) { return this.put(`/admin/users/${userId}/toggle-status`); }
    getAdminReports(page = 0) { return this.get(`/admin/reports?page=${page}`); }
}

const api = new ApiClient();
