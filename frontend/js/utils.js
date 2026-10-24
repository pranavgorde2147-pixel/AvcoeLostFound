function showToast(message, type = 'info') {
    let container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    const icons = { success: '✓', error: '✗', info: 'ℹ', warning: '⚠' };
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `<span>${icons[type] || 'ℹ'}</span><span>${message}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.animation = 'fadeOut 0.3s ease forwards';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

function formatDate(dateStr) {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    const now = new Date();
    const diff = now - date;
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);

    if (minutes < 1) return 'Just now';
    if (minutes < 60) return `${minutes}m ago`;
    if (hours < 24) return `${hours}h ago`;
    if (days < 7) return `${days}d ago`;
    return date.toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });
}

function formatTime(dateStr) {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' });
}

function formatDateTime(dateStr) {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleString('en-IN', {
        day: 'numeric', month: 'short', year: 'numeric',
        hour: '2-digit', minute: '2-digit'
    });
}

function getStatusBadge(status) {
    const map = {
        'ACTIVE': '<span class="badge badge-active">Active</span>',
        'LOST': '<span class="badge badge-lost">Lost</span>',
        'FOUND': '<span class="badge badge-found">Found</span>',
        'RETURNED': '<span class="badge badge-returned">Returned</span>',
        'CLOSED': '<span class="badge" style="background:var(--text-muted);color:white;">Closed</span>',
        'CLAIM_IN_PROGRESS': '<span class="badge badge-pending">Claim in Progress</span>',
        'OWNER_VERIFIED': '<span class="badge badge-verified">Owner Verified</span>',
        'VERIFICATION_IN_PROGRESS': '<span class="badge badge-pending">Verifying</span>',
        'PENDING': '<span class="badge badge-pending">Pending</span>',
        'REJECTED': '<span class="badge" style="background:var(--danger);color:white;">Declined</span>',
        'COMPLETED': '<span class="badge badge-verified">Completed</span>',
        'SCHEDULED': '<span class="badge badge-active">Scheduled</span>',
        'CANCELLED': '<span class="badge" style="background:var(--text-muted);color:white;">Cancelled</span>',
    };
    return map[status] || `<span class="badge">${status}</span>`;
}

function getCategoryIcon(category) {
    const icons = {
        'ELECTRONICS': '📱', 'BAGS': '🎒', 'BOOKS': '📚', 'ID_CARDS': '🪪',
        'WALLETS': '👛', 'KEYS': '🔑', 'DOCUMENTS': '📄', 'CLOTHING': '👕',
        'ACCESSORIES': '⌚', 'STATIONERY': '✏️', 'OTHER': '📦'
    };
    return icons[category] || '📦';
}

function getLocationIcon() { return '📍'; }

function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => { clearTimeout(timeout); func(...args); };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function initSidebar() {
    const user = auth.getUser();
    if (!user) return;

    const sidebarNav = document.querySelector('.sidebar-nav');
    if (!sidebarNav) return;

    const navItems = [
        { href: '/dashboard.html', icon: '🏠', label: 'Dashboard' },
        { href: '/lost-items.html', icon: '🔍', label: 'Lost Items' },
        { href: '/found-items.html', icon: '📦', label: 'Found Items' },
        { href: '/messages.html', icon: '💬', label: 'Messages' },
        { section: 'My Items' },
        { href: '/create-lost.html', icon: '📌', label: 'Report Lost' },
        { href: '/create-found.html', icon: '✅', label: 'Report Found' },
    ];

    if (auth.isAdmin()) {
        navItems.push({ section: 'Admin' });
        navItems.push({ href: '/admin.html', icon: '⚙️', label: 'Admin Panel' });
    }

    const currentPath = window.location.pathname;
    let html = '';

    for (const item of navItems) {
        if (item.section) {
            html += `<div class="sidebar-section"><div class="sidebar-section-title">${item.section}</div></div>`;
        } else {
            const isActive = currentPath === item.href ? 'active' : '';
            html += `<a href="${item.href}" class="nav-item ${isActive}"><span class="nav-item-icon">${item.icon}</span>${item.label}</a>`;
        }
    }

    sidebarNav.innerHTML = html;

    // Update user info
    const userNameEl = document.querySelector('.sidebar-user-name');
    const userRoleEl = document.querySelector('.sidebar-user-role');
    const userAvatarEl = document.querySelector('.sidebar-user .avatar');
    if (userNameEl) userNameEl.textContent = user.fullName;
    if (userRoleEl) userRoleEl.textContent = user.role.toLowerCase();
    if (userAvatarEl) {
        userAvatarEl.textContent = auth.getInitials(user.fullName);
        userAvatarEl.style.background = auth.getAvatarColor(user.fullName);
    }

    // Load unread count
    loadUnreadCount();
}

async function loadUnreadCount() {
    try {
        const count = await api.getUnreadCount();
        const badge = document.querySelector('.notification-badge');
        if (badge) {
            badge.textContent = count;
            badge.style.display = count > 0 ? 'block' : 'none';
        }
        // Update sidebar badge
        const msgBadge = document.querySelector('.nav-item[href="/messages.html"] .nav-item-badge');
        if (msgBadge) {
            msgBadge.textContent = count;
            msgBadge.style.display = count > 0 ? 'inline' : 'none';
        }
    } catch (e) { /* ignore */ }
}

function initMobileNav() {
    const user = auth.getUser();
    if (!user) return;
    const currentPath = window.location.pathname;
    const navItems = [
        { href: '/dashboard.html', icon: '🏠', label: 'Home' },
        { href: '/lost-items.html', icon: '🔍', label: 'Lost' },
        { href: '/found-items.html', icon: '📦', label: 'Found' },
        { href: '/messages.html', icon: '💬', label: 'Chat' },
        { href: '/profile.html', icon: '👤', label: 'Profile' },
    ];
    const mobileNav = document.querySelector('.mobile-nav-items');
    if (mobileNav) {
        mobileNav.innerHTML = navItems.map(item =>
            `<a href="${item.href}" class="mobile-nav-item ${currentPath === item.href ? 'active' : ''}">
                <span class="mobile-nav-item-icon">${item.icon}</span>${item.label}
            </a>`
        ).join('');
    }
}

function toggleSidebar() {
    document.querySelector('.sidebar')?.classList.toggle('open');
    document.querySelector('.sidebar-overlay')?.classList.toggle('active');
}

function showModal(title, contentHtml, footerHtml = '') {
    const existing = document.querySelector('.modal-overlay');
    if (existing) existing.remove();

    const overlay = document.createElement('div');
    overlay.className = 'modal-overlay';
    overlay.innerHTML = `
        <div class="modal">
            <div class="modal-header">
                <div class="modal-title">${title}</div>
                <button class="modal-close" onclick="this.closest('.modal-overlay').remove()">✕</button>
            </div>
            <div class="modal-body">${contentHtml}</div>
            ${footerHtml ? `<div class="modal-footer">${footerHtml}</div>` : ''}
        </div>
    `;
    overlay.addEventListener('click', (e) => {
        if (e.target === overlay) overlay.remove();
    });
    document.body.appendChild(overlay);
    return overlay;
}

function closeModal() {
    document.querySelector('.modal-overlay')?.remove();
}
