import { loanTable } from './loan-view.js';
import { mountPage } from './ui.js';

if (mountPage()) {
    loanTable('my-active', {
        path: '/transactions/me', filters: () => ({ activeOnly: true }), staff: false,
        empty: 'You have no books checked out.',
    }).load();
    loanTable('my-history', {
        path: '/transactions/me', staff: false, empty: 'You have no loan history.',
    }).load();
    loanTable('my-fines', {
        path: '/transactions/me/fines', staff: false, empty: 'You have no fines.', finesOnly: true,
    }).load();
}
