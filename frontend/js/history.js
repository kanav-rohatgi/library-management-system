import { allPages } from './api.js';
import { loanTable } from './loan-view.js';
import { bookOption, mountPage, setOptions, studentOption } from './ui.js';

async function prepareFilters(form) {
    try {
        const [students, books] = await Promise.all([allPages('/students'), allPages('/books')]);
        setOptions(form.elements.studentId, students, 'All students', studentOption);
        setOptions(form.elements.bookId, books, 'All books', bookOption);
    } catch (error) {
        document.getElementById('history-error').textContent = error.message;
    }
}

if (mountPage()) {
    const form = document.getElementById('history-filters');
    let criteria = {};
    const table = loanTable('history-table', {
        path: '/transactions', filters: () => criteria, empty: 'No transactions match these filters.',
    });
    form.addEventListener('submit', (event) => {
        event.preventDefault();
        criteria = Object.fromEntries(new FormData(form));
        table.load(0);
    });
    prepareFilters(form);
    table.load();
}
