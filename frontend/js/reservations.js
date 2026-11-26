import { api } from './api.js';
import { openForm } from './dialog.js';
import { dataTable } from './table.js';
import { badge, button, date, mountPage, toast } from './ui.js';

function run(identity) {
    const columns = [['Book', (reservation) => reservation.bookTitle]];
    if (identity.role !== 'STUDENT') columns.push(['Student', (reservation) => reservation.studentName]);
    columns.push(
        ['Reserved', (reservation) => date(reservation.reservedAt)],
        ['Status', (reservation) => {
            const states = { PENDING: ['Pending', 'warn'], FULFILLED: ['Fulfilled', 'good'], CANCELLED: ['Cancelled', ''] };
            return badge(...states[reservation.status]);
        }],
        ['Actions', (reservation) => reservation.status === 'PENDING' ? button('Cancel', {
            style: 'btn-secondary btn-small',
            action: () => openForm({
                title: 'Cancel reservation',
                description: 'Cancel the reservation for “' + reservation.bookTitle + '”?',
                submitLabel: 'Cancel reservation', danger: true,
                save: async () => {
                    await api('/reservations/' + reservation.id, { method: 'DELETE' });
                    toast('Reservation cancelled.');
                    table.load();
                },
            }),
        }) : '—'],
    );
    const table = dataTable('reservations-table', {
        columns, empty: 'No reservations.',
        fetchPage: (params, signal) => api('/reservations', { params, signal }),
    });
    table.load();
}

const identity = mountPage();
if (identity) run(identity);
