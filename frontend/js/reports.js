import { loanTable } from './loan-view.js';
import { mountPage } from './ui.js';

if (mountPage()) {
    loanTable('overdue-table', { path: '/transactions/overdue', empty: 'No overdue loans.' }).load();
    loanTable('fines-table', { path: '/transactions/fines', empty: 'No fines to report.', finesOnly: true }).load();
}
