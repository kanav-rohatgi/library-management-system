import { button, element, refreshIcons } from './ui.js';

export function dataTable(id, { columns, fetchPage, empty = 'No results.', size = 10, paginate = true }) {
    const root = document.getElementById(id);
    const wrap = element('div', 'table-wrap');
    wrap.tabIndex = 0;
    const table = element('table');
    const head = table.createTHead().insertRow();
    for (const [label] of columns) {
        const cell = element('th', '', label);
        cell.scope = 'col';
        head.append(cell);
    }
    const body = table.createTBody();
    wrap.append(table);
    root.append(wrap);

    const pager = element('div', 'pagination');
    const count = element('span', 'small');
    count.setAttribute('aria-live', 'polite');
    const controls = element('div', 'button-group');
    const previous = button('Previous', { style: 'btn-secondary btn-small', action: () => load(page - 1) });
    const next = button('Next', { style: 'btn-secondary btn-small', action: () => load(page + 1) });
    controls.append(previous, next);
    pager.append(count, controls);
    if (paginate) root.append(pager);

    let page = 0;
    let activeRequest;

    function message(text, alert = false) {
        const row = element('tr');
        const cell = element('td', 'table-status', text);
        cell.colSpan = columns.length;
        cell.setAttribute('role', alert ? 'alert' : 'status');
        row.append(cell);
        body.replaceChildren(row);
    }

    async function load(requested = page) {
        activeRequest?.abort();
        const controller = new AbortController();
        activeRequest = controller;
        previous.disabled = next.disabled = true;
        root.setAttribute('aria-busy', 'true');
        count.textContent = '';
        message('Loading…');

        try {
            const result = await fetchPage({ page: requested, size }, controller.signal);
            if (controller.signal.aborted) return;
            if (requested > 0 && result.content.length === 0) {
                return load(Math.max(0, result.totalPages - 1));
            }
            page = result.page;
            body.replaceChildren();
            for (const item of result.content) {
                const row = body.insertRow();
                for (const [, display] of columns) {
                    const value = display(item);
                    const cell = row.insertCell();
                    if (value instanceof Node) cell.append(value);
                    else cell.textContent = value ?? '—';
                }
            }
            if (!result.content.length) message(empty);
            count.textContent = result.totalElements
                ? 'Page ' + (page + 1) + ' of ' + result.totalPages + ' · ' + result.totalElements + ' results'
                : '0 results';
            previous.disabled = page === 0;
            next.disabled = page + 1 >= result.totalPages;
            refreshIcons();
        } catch (error) {
            if (!controller.signal.aborted) message(error.message, true);
        } finally {
            if (!controller.signal.aborted) root.removeAttribute('aria-busy');
        }
    }

    return { load };
}
