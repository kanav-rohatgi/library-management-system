import { allPages, api } from './api.js';
import { openForm } from './dialog.js';
import { loanTable } from './loan-view.js';
import { bookOption, button, money, mountPage, setOptions, showFormErrors, studentOption, toast } from './ui.js';

function run() {
    const form = document.getElementById('issue-form');
    const feedback = document.getElementById('issue-error');
    const submit = form.querySelector('button[type="submit"]');
    const submitLabel = submit.querySelector("span");
    const students = form.elements.studentId;
    const books = form.elements.bookId;
    const table = loanTable('loans-table', {
        path: '/transactions', filters: () => ({ activeOnly: true }), empty: 'No active loans.',
        extra: [['Actions', (loan) => button('Return', {
            style: 'btn-secondary btn-small',
            action: () => openForm({
                title: 'Return book',
                description: 'Check in “' + loan.bookTitle + '” from ' + loan.studentName + '?',
                submitLabel: 'Confirm return',
                save: async () => {
                    const returned = await api('/transactions/' + loan.id + '/return', { method: 'POST' });
                    toast(returned.fineAmount > 0 ? 'Book returned. Fine recorded: ' + money(returned.fineAmount) + '.' : 'Book returned.');
                    table.load();
                    loadChoices();
                },
            }),
        })]],
    });

    let choiceVersion = 0;
    async function loadChoices() {
        const version = ++choiceVersion;
        submit.disabled = true;
        try {
            const [studentList, bookList] = await Promise.all([allPages('/students'), allPages('/books', { available: true })]);
            if (version !== choiceVersion) return;
            const active = studentList.filter((student) => student.status === 'ACTIVE');
            setOptions(students, active, active.length ? 'Choose a student' : 'No active students', studentOption);
            setOptions(books, bookList, bookList.length ? 'Choose a book' : 'No copies available', bookOption);
            students.disabled = active.length === 0;
            books.disabled = bookList.length === 0;
            submit.disabled = students.disabled || books.disabled;
        } catch (error) {
            if (version === choiceVersion) showFormErrors(form, feedback, error);
        }
    }

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        if (submit.disabled) return;
        showFormErrors(form, feedback, null);
        const body = { bookId: Number(books.value), studentId: Number(students.value) };
        submit.disabled = true;
        submitLabel.textContent = 'Issuing…';
        try {
            await api('/transactions/issue', { method: 'POST', body });
            toast('Book issued.');
            form.reset();
            table.load(0);
            await loadChoices();
        } catch (error) {
            showFormErrors(form, feedback, error);
            submit.disabled = false;
        } finally {
            submitLabel.textContent = 'Issue book';
        }
    });

    loadChoices();
    table.load();
}

if (mountPage()) run();
