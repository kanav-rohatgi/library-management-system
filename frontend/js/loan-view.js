import { api } from './api.js';
import { dataTable } from './table.js';
import { badge, date, detail, money } from './ui.js';

export function loanStatus(loan) {
    if (loan.returnDate) return badge('Returned', 'good');
    if (loan.overdue) return badge('Overdue', 'danger');
    return badge('On loan', 'info');
}

export function loanTable(id, { path, filters = () => ({}), staff = true, extra = [], empty, finesOnly = false }) {
    const columns = [['Book', (loan) => loan.bookTitle]];
    if (staff) columns.push(['Student', (loan) => loan.studentName]);
    if (!finesOnly) columns.push(['Issued', (loan) => date(loan.issueDate)]);
    columns.push(
        ['Due date', (loan) => date(loan.dueDate)],
        ['Returned', (loan) => date(loan.returnDate)],
        ['Status', loanStatus],
        ['Fine', (loan) => detail(money(loan.returnDate ? loan.fineAmount : loan.accruedFine),
            loan.returnDate ? 'Recorded' : 'Accrued')],
        ...extra,
    );
    return dataTable(id, {
        columns, empty,
        fetchPage: (page, signal) => api(path, { params: { ...filters(), ...page }, signal }),
    });
}
