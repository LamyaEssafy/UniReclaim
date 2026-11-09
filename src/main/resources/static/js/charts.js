/**
 * Premium Chart configurations using Chart.js
 */

// Define global colors matching the design system
const chartColors = {
    primary: '#4361ee',
    secondary: '#7209b7',
    success: '#06d6a0',
    warning: '#ffd166',
    danger: '#ef476f',
    info: '#118ab2',
    grey: '#94a3b8',
    palette: ['#4361ee', '#7209b7', '#f72585', '#4cc9f0', '#06d6a0', '#ffd166', '#ef476f', '#118ab2']
};

// Global Chart defaults
if (window.Chart) {
    Chart.defaults.font.family = "'Inter', sans-serif";
    Chart.defaults.color = '#475569'; // text-secondary
    Chart.defaults.responsive = true;
    Chart.defaults.maintainAspectRatio = false;
}

/**
 * Initialize a Doughnut / Pie Chart
 */
function initDoughnutChart(canvasId, labels, data) {
    const ctx = document.getElementById(canvasId);
    if (!ctx) return null;
    
    return new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: data,
                backgroundColor: chartColors.palette.slice(0, labels.length),
                borderWidth: 2,
                borderColor: '#ffffff'
            }]
        },
        options: {
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        padding: 15,
                        usePointStyle: true,
                        boxWidth: 8
                    }
                }
            },
            cutout: '65%'
        }
    });
}

/**
 * Initialize a Vertical Bar Chart
 */
function initBarChart(canvasId, labels, data) {
    const ctx = document.getElementById(canvasId);
    if (!ctx) return null;
    
    return new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Réclamations',
                data: data,
                backgroundColor: 'rgba(67, 97, 238, 0.85)',
                borderColor: chartColors.primary,
                borderWidth: 0,
                borderRadius: 6,
                borderSkipped: false
            }]
        },
        options: {
            scales: {
                x: {
                    grid: {
                        display: false
                    }
                },
                y: {
                    beginAtZero: true,
                    grid: {
                        color: '#f1f5f9'
                    },
                    ticks: {
                        precision: 0
                    }
                }
            },
            plugins: {
                legend: {
                    display: false
                }
            }
        }
    });
}

/**
 * Initialize a Horizontal Bar Chart
 */
function initHorizontalBarChart(canvasId, labels, data) {
    const ctx = document.getElementById(canvasId);
    if (!ctx) return null;
    
    return new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Sollicitations',
                data: data,
                backgroundColor: 'rgba(114, 9, 183, 0.85)',
                borderColor: chartColors.secondary,
                borderWidth: 0,
                borderRadius: 6,
                borderSkipped: false
            }]
        },
        options: {
            indexAxis: 'y',
            scales: {
                x: {
                    beginAtZero: true,
                    grid: {
                        color: '#f1f5f9'
                    },
                    ticks: {
                        precision: 0
                    }
                },
                y: {
                    grid: {
                        display: false
                    }
                }
            },
            plugins: {
                legend: {
                    display: false
                }
            }
        }
    });
}

/**
 * Initialize a Line Chart
 */
function initLineChart(canvasId, labels, data) {
    const ctx = document.getElementById(canvasId);
    if (!ctx) return null;
    
    return new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Volume',
                data: data,
                fill: true,
                backgroundColor: 'rgba(67, 97, 238, 0.05)',
                borderColor: chartColors.primary,
                tension: 0.4,
                borderWidth: 3,
                pointBackgroundColor: chartColors.primary,
                pointHoverRadius: 7
            }]
        },
        options: {
            scales: {
                x: {
                    grid: {
                        display: false
                    }
                },
                y: {
                    beginAtZero: true,
                    grid: {
                        color: '#f1f5f9'
                    },
                    ticks: {
                        precision: 0
                    }
                }
            },
            plugins: {
                legend: {
                    display: false
                }
            }
        }
    });
}

// Automatically bind charts if canvases exist with data elements
document.addEventListener('DOMContentLoaded', () => {
    // Look for canvas configs in the HTML page
    const doughnutCanvas = document.querySelector('canvas[data-chart-type="doughnut"]');
    if (doughnutCanvas) {
        try {
            const labels = JSON.parse(doughnutCanvas.dataset.chartLabels);
            const values = JSON.parse(doughnutCanvas.dataset.chartValues);
            initDoughnutChart(doughnutCanvas.id, labels, values);
        } catch (e) {
            console.error('Error loading doughnut chart data', e);
        }
    }
    
    const barCanvas = document.querySelector('canvas[data-chart-type="bar"]');
    if (barCanvas) {
        try {
            const labels = JSON.parse(barCanvas.dataset.chartLabels);
            const values = JSON.parse(barCanvas.dataset.chartValues);
            initBarChart(barCanvas.id, labels, values);
        } catch (e) {
            console.error('Error loading bar chart data', e);
        }
    }
});
