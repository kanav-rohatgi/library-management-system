import { api } from './api.js';
import { openForm } from './dialog.js';
import { dataTable } from './table.js';
import { badge, button, detail, element, mountPage, pageAction, toast } from './ui.js';

const fields = [
    { name: 'title', label: 'Title', maxLength: 200 },
    { name: 'author', label: 'Author', maxLength: 160 },
    { name: 'isbn', label: 'ISBN', maxLength: 20 },
    { name: 'category', label: 'Category', maxLength: 80 },
    { name: 'publisher', label: 'Publisher', maxLength: 160 },
    { name: 'publicationYear', label: 'Publication year', type: 'number', min: 1 },
    { name: 'totalCopies', label: 'Total copies', type: 'number', min: 0 },
];

function run(identity) {
    const isStudent = identity.role === 'STUDENT';
    const filters = document.getElementById('book-filters');
    let criteria = Object.fromEntries(new FormData(filters));
    const reservedHere = new Set();

    const table = dataTable('books-table', {
        empty: 'No books match these filters.',
        columns: [
            ['Title', (book) => detail(book.title, book.author)],
            ['ISBN', (book) => book.isbn],
            ['Category', (book) => book.category],
            ['Publication', (book) => detail(book.publisher, book.publicationYear)],
            ['Availability', (book) => book.availableCopies
                ? badge(book.availableCopies + ' / ' + book.totalCopies + ' available', 'good')
                : badge('Unavailable', 'warn')],
            ['Actions', actions],
        ],
        fetchPage: (page, signal) => api('/books', { params: { ...criteria, ...page }, signal }),
    });

    function edit(book) {
        openForm({
            title: book ? 'Edit book' : 'Add book',
            fields: fields.map((field) => ({ ...field, value: book?.[field.name] })),
            submitLabel: book ? 'Save changes' : 'Add book',
            save: async (values) => {
                await api(book ? '/books/' + book.id : '/books', { method: book ? 'PUT' : 'POST', body: values });
                toast(book ? 'Book updated.' : 'Book added.');
                table.load(book ? undefined : 0);
            },
        });
    }

    function actions(book) {
        const group = element('div', 'button-group');
        if (isStudent) {
            if (book.availableCopies || reservedHere.has(book.id)) return '—';
            return button('Reserve', {
                style: 'btn-secondary btn-small', glyph: 'bookmark',
                action: () => openForm({
                    title: 'Reserve book', description: book.title, submitLabel: 'Reserve',
                    save: async () => {
                        await api('/reservations', { method: 'POST', body: { bookId: book.id } });
                        reservedHere.add(book.id);
                        toast('Book reserved.');
                        table.load();
                    },
                }),
            });
        }

        group.append(
            button('Edit', { style: 'btn-secondary btn-small', action: () => edit(book) }),
            button('Delete', {
                style: 'btn-secondary btn-small',
                action: () => openForm({
                    title: 'Delete book',
                    description: 'Remove “' + book.title + '” from the catalogue?',
                    submitLabel: 'Delete book', danger: true,
                    save: async () => {
                        await api('/books/' + book.id, { method: 'DELETE' });
                        toast('Book deleted.');
                        table.load();
                    },
                }),
            }),
        );
        return group;
    }

    if (!isStudent) pageAction(button('Add book', { glyph: 'plus', action: () => edit() }));
    filters.addEventListener('submit', (event) => {
        event.preventDefault();
        criteria = Object.fromEntries(new FormData(filters));
        table.load(0);
    });
    table.load();
}

const identity = mountPage();
if (identity) run(identity);
