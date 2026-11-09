/**
 * UniReclaim Main Application JavaScript
 */

document.addEventListener('DOMContentLoaded', () => {
    // Sidebar toggle functionality
    const sidebar = document.getElementById('sidebar');
    const toggleBtn = document.querySelector('.sidebar-toggle-btn');
    
    if (toggleBtn && sidebar) {
        toggleBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            sidebar.classList.toggle('active');
            
            // On desktop, toggle collapse
            if (window.innerWidth >= 992) {
                document.body.classList.toggle('sidebar-collapsed');
            }
        });
        
        // Close sidebar on mobile when clicking outside
        document.addEventListener('click', (e) => {
            if (window.innerWidth < 992) {
                if (!sidebar.contains(e.target) && !toggleBtn.contains(e.target)) {
                    sidebar.classList.remove('active');
                }
            }
        });
    }

    // Auto-dismiss alert banners after 5 seconds
    const alerts = document.querySelectorAll('.alert-dismissible');
    alerts.forEach(alert => {
        setTimeout(() => {
            const closeBtn = alert.querySelector('.btn-close');
            if (closeBtn) {
                closeBtn.click();
            }
        }, 5000);
    });

    // Confirm deletion actions
    const deleteLinks = document.querySelectorAll('.confirm-delete');
    deleteLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            if (!confirm('Êtes-vous sûr de vouloir supprimer cet élément ? Cette action est irréversible.')) {
                e.preventDefault();
            }
        });
    });

    // Handle conditional fields in forms
    const categorySelect = document.getElementById('categorySelect');
    const moduleSelectWrapper = document.getElementById('moduleSelectWrapper');
    
    if (categorySelect && moduleSelectWrapper) {
        const toggleModuleField = () => {
            const selectedText = categorySelect.options[categorySelect.selectedIndex].text.toLowerCase();
            // Show module selection for note or absence related complaints
            if (selectedText.includes('note') || selectedText.includes('absence') || selectedText.includes('module')) {
                moduleSelectWrapper.style.display = 'block';
                moduleSelectWrapper.querySelector('select').setAttribute('required', 'required');
            } else {
                moduleSelectWrapper.style.display = 'none';
                moduleSelectWrapper.querySelector('select').removeAttribute('required');
            }
        };
        
        categorySelect.addEventListener('change', toggleModuleField);
        // Run on load
        toggleModuleField();
    }

    // Client-side search/filter for tables
    const searchInput = document.querySelector('.header-search input');
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            const query = e.target.value.toLowerCase().trim();
            const tables = document.querySelectorAll('table');
            
            tables.forEach(table => {
                const rows = table.querySelectorAll('tbody tr');
                let matchesFound = 0;
                
                rows.forEach(row => {
                    if (row.querySelector('td[colspan]') && !row.classList.contains('search-empty-row')) {
                        // Keep the initial "no data" row hidden if searching, or shown if empty query
                        row.style.display = query === '' ? '' : 'none';
                        return;
                    }
                    if (row.classList.contains('search-empty-row')) {
                        return;
                    }
                    
                    const text = row.textContent.toLowerCase();
                    if (text.includes(query)) {
                        row.style.display = '';
                        matchesFound++;
                    } else {
                        row.style.display = 'none';
                    }
                });
                
                let emptyRow = table.querySelector('.search-empty-row');
                if (matchesFound === 0 && query !== '') {
                    if (!emptyRow) {
                        emptyRow = document.createElement('tr');
                        emptyRow.className = 'search-empty-row';
                        const colsCount = table.querySelectorAll('thead th').length || 6;
                        emptyRow.innerHTML = `<td colspan="${colsCount}" class="text-center py-4 text-muted">
                            <i class="fas fa-search-minus fa-2x mb-2 d-block"></i>
                            Aucun résultat ne correspond à votre recherche "${e.target.value}"
                        </td>`;
                        table.querySelector('tbody').appendChild(emptyRow);
                    } else {
                        emptyRow.style.display = '';
                    }
                } else if (emptyRow) {
                    emptyRow.style.display = 'none';
                }
            });
        });
    }
});

// Helper functions
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    if (sidebar) {
        sidebar.classList.toggle('active');
    }
}
