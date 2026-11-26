import { api } from './api.js';
import { openForm } from './dialog.js';
import { dataTable } from './table.js';
import { button, element, mountPage, pageAction, toast } from './ui.js';

const username = { name: 'username', label: 'Username', maxLength: 80, pattern: '[A-Za-z0-9._\\-]+' };
const password = { name: 'password', label: 'Password', type: 'password', minLength: 8, maxLength: 72 };

function run() {
    const table = dataTable('librarians-table', {
        columns: [['Username', (account) => account.username], ['Actions', actions]],
        empty: 'No librarian accounts.',
        paginate: false,
        fetchPage: async (_params, signal) => {
            const content = await api('/librarians', { signal });
            return { content, page: 0, totalPages: 1, totalElements: content.length };
        },
    });

    function actions(account) {
        const group = element('div', 'button-group');
        group.append(
            button('Reset password', {
                style: 'btn-secondary btn-small',
                action: () => openForm({
                    title: 'Reset password', description: account.username, fields: [password], submitLabel: 'Save password',
                    save: async (body) => {
                        await api('/librarians/' + account.id + '/password', { method: 'PUT', body });
                        toast('Password updated.');
                    },
                }),
            }),
            button('Remove', {
                style: 'btn-secondary btn-small',
                action: () => openForm({
                    title: 'Remove librarian', description: 'Remove access for ' + account.username + '?',
                    submitLabel: 'Remove access', danger: true,
                    save: async () => {
                        await api('/librarians/' + account.id, { method: 'DELETE' });
                        toast('Librarian removed.');
                        table.load();
                    },
                }),
            }),
        );
        return group;
    }

    pageAction(button('Add librarian', {
        glyph: 'plus',
        action: () => openForm({
            title: 'Add librarian', fields: [username, password], submitLabel: 'Create account',
            save: async (body) => {
                await api('/librarians', { method: 'POST', body });
                toast('Librarian account created.');
                table.load();
            },
        }),
    }));
    table.load();
}

if (mountPage()) run();
