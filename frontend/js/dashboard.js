import { api } from './api.js';
import { element, icon, money, mountPage, refreshIcons } from './ui.js';

const metricDefinitions = [
    ['Total books', 'totalBooks', 'library-big'],
    ['Students', 'totalStudents', 'users-round'],
    ['Active loans', 'issued', 'book-open'],
    ['Overdue', 'overdue', 'calendar-clock'],
    ['Recorded fines', 'totalFines', 'wallet'],
];

async function loadDashboard() {
    const status = document.getElementById('dashboard-status');
    try {
        const stats = await api('/dashboard');
        const metrics = document.getElementById('metrics');
        for (const [label, key, glyph] of metricDefinitions) {
            const card = element('article', 'panel metric');
            const mark = element('span', 'metric-icon');
            mark.append(icon(glyph));
            const value = key === 'totalFines' ? money(stats[key]) : stats[key].toLocaleString('en-IN');
            card.append(mark, element('p', 'metric-label', label), element('p', 'metric-value', value));
            metrics.append(card);
        }
        metrics.hidden = false;
        status.hidden = true;
        refreshIcons();
        renderCharts(stats);
    } catch (error) {
        status.hidden = false;
        status.textContent = error.message;
        status.className = 'error-text';
        status.setAttribute('role', 'alert');
    }
}

function renderCharts(stats) {
    const categories = Object.entries(stats.booksByCategory);
    const outstanding = Math.max(0, stats.issued - stats.overdue);
    const loans = [['On time', outstanding], ['Overdue', stats.overdue], ['Returned', stats.returned]];
    document.getElementById('category-summary').textContent = categories.length
        ? categories.map(([label, count]) => label + ' ' + count).join(' · ') : 'No books in the catalogue.';
    document.getElementById('loan-summary').textContent = loans.map(([label, count]) => label + ' ' + count).join(' · ');
    document.getElementById('charts').hidden = false;

    if (!window.Chart) {
        document.querySelectorAll('.chart').forEach((chart) => { chart.hidden = true; });
        return;
    }
    const palette = ['#8461ca', '#eeaa62', '#a5d3c0'];
    const settings = { responsive: true, maintainAspectRatio: false };
    const categoriesCanvas = document.getElementById('categories');
    if (categories.length) {
        new window.Chart(categoriesCanvas, {
            type: 'bar',
            data: {
                labels: categories.map(([label]) => label),
                datasets: [{ label: 'Books', data: categories.map(([, count]) => count), backgroundColor: '#8461ca', borderRadius: 6 }],
            },
            options: {
                ...settings, indexAxis: 'y', plugins: { legend: { display: false } },
                scales: { x: { beginAtZero: true, ticks: { precision: 0 } }, y: { grid: { display: false } } },
            },
        });
    } else {
        categoriesCanvas.parentElement.hidden = true;
    }

    const statusCanvas = document.getElementById('loan-status');
    if (loans.some(([, count]) => count > 0)) {
        new window.Chart(statusCanvas, {
            type: 'doughnut',
            data: {
                labels: loans.map(([label]) => label),
                datasets: [{ data: loans.map(([, count]) => count), backgroundColor: palette, borderWidth: 2 }],
            },
            options: { ...settings, cutout: '70%', plugins: { legend: { position: 'bottom' } } },
        });
    } else {
        statusCanvas.parentElement.hidden = true;
    }
}

if (mountPage()) loadDashboard();
