class AuthManager {
    constructor() {
        this.user = this.loadUser();
    }

    loadUser() {
        try {
            const userData = localStorage.getItem('user');
            return userData ? JSON.parse(userData) : null;
        } catch {
            return null;
        }
    }

    isLoggedIn() {
        return !!localStorage.getItem('token') && !!this.user;
    }

    getToken() {
        return localStorage.getItem('token');
    }

    getUser() {
        return this.user;
    }

    setAuth(data) {
        localStorage.setItem('token', data.token);
        localStorage.setItem('user', JSON.stringify({
            id: data.id,
            email: data.email,
            fullName: data.fullName,
            role: data.role,
            profileImageUrl: data.profileImageUrl
        }));
        this.user = this.loadUser();
    }

    logout() {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        this.user = null;
        window.location.href = '/login.html';
    }

    requireAuth() {
        if (!this.isLoggedIn()) {
            window.location.href = '/login.html';
            return false;
        }
        return true;
    }

    requireAdmin() {
        if (!this.requireAuth()) return false;
        if (this.user.role !== 'ADMIN') {
            window.location.href = '/dashboard.html';
            return false;
        }
        return true;
    }

    isAdmin() {
        return this.user && this.user.role === 'ADMIN';
    }

    getInitials(name) {
        if (!name) return '?';
        return name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
    }

    getAvatarColor(name) {
        if (!name) return '#6366f1';
        const colors = ['#6366f1', '#8b5cf6', '#ec4899', '#ef4444', '#f59e0b', '#22c55e', '#3b82f6', '#06b6d4'];
        let hash = 0;
        for (let i = 0; i < name.length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash);
        return colors[Math.abs(hash) % colors.length];
    }
}

const auth = new AuthManager();
