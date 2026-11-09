/**
 * AJAX in-app notifications helper
 */

document.addEventListener('DOMContentLoaded', () => {
    const notificationBell = document.getElementById('notificationBell');
    const badge = document.querySelector('.notification-badge');
    const dropdown = document.querySelector('.notification-dropdown');

    if (!notificationBell) return;

    // Load initial notifications
    const loadNotifications = () => {
        fetch('/notifications/api/notifications')
            .then(res => {
                if (res.ok) return res.json();
                throw new Error('Network error');
            })
            .then(data => {
                // Update badge
                if (data.length > 0) {
                    if (badge) {
                        badge.innerText = data.length;
                        badge.style.display = 'flex';
                    }
                } else {
                    if (badge) badge.style.display = 'none';
                }

                // Render in dropdown
                const listItems = dropdown.querySelectorAll('li:not(.dropdown-header):not(.dropdown-divider)');
                listItems.forEach(item => item.remove());

                if (data.length === 0) {
                    const emptyItem = document.createElement('li');
                    emptyItem.className = 'notification-empty text-center py-4 text-muted';
                    emptyItem.innerHTML = `
                        <i class="fas fa-bell-slash fa-2x mb-2 d-block"></i>
                        <span>Aucune nouvelle notification</span>
                    `;
                    dropdown.appendChild(emptyItem);
                } else {
                    const parseDate = (dateVal) => {
                        if (!dateVal) return new Date();
                        if (Array.isArray(dateVal)) {
                            return new Date(dateVal[0], dateVal[1] - 1, dateVal[2], dateVal[3] || 0, dateVal[4] || 0);
                        }
                        return new Date(dateVal);
                    };

                    data.forEach(notif => {
                        const li = document.createElement('li');
                        li.className = 'notification-item p-3 border-bottom';
                        li.style.cursor = 'pointer';
                        li.innerHTML = `
                            <div class="d-flex justify-content-between">
                                <span class="small font-weight-bold text-dark">${notif.message}</span>
                            </div>
                            <small class="text-muted d-block mt-1">${parseDate(notif.dateNotification).toLocaleDateString('fr-FR', {hour: '2-digit', minute:'2-digit'})}</small>
                        `;
                        
                        // Clicking marks as read and redirects
                        li.addEventListener('click', () => {
                            fetch(`/notifications/api/notifications/${notif.id}/lire`, {
                                method: 'POST'
                            }).then(() => {
                                window.location.href = notif.lien;
                            });
                        });
                        dropdown.appendChild(li);
                    });
                }
            })
            .catch(err => console.error('Error fetching notifications:', err));
    };

    // Poll every 30 seconds
    setInterval(loadNotifications, 30000);
    // Load once on start
    loadNotifications();

    // Mark all as read click handler
    const markAllReadBtn = dropdown.querySelector('.dropdown-header a');
    if (markAllReadBtn) {
        markAllReadBtn.addEventListener('click', (e) => {
            e.preventDefault();
            fetch('/notifications/api/notifications/lire-tout', {
                method: 'POST'
            })
            .then(() => {
                loadNotifications();
            })
            .catch(err => console.error('Error marking all notifications read:', err));
        });
    }
});
