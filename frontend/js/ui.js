import { home, session, signOut } from './auth.js';

const STAFF = ['ADMIN', 'LIBRARIAN'];
const EVERYONE = [...STAFF, 'STUDENT'];
const GROUPS = [
    ['Library', [
        ['dashboard', 'Dashboard', 'layout-dashboard', STAFF],
        ['books', 'Books', 'library-big', EVERYONE],
        ['students', 'Students', 'users-round', STAFF],
        ['my-books', 'My books', 'book-open', ['STUDENT']],
    ]],
    ['Circulation', [
        ['circulation', 'Issue and return', 'arrow-left-right', STAFF],
        ['history', 'Transaction history', 'history', STAFF],
        ['reservations', 'Reservations', 'bookmark', EVERYONE],
        ['reports', 'Overdue and fines', 'clipboard-list', STAFF],
    ]],
    ['Administration', [
        ['librarians', 'Librarians', 'shield-user', ['ADMIN']],
    ]],
];

export function element(tag, className, text) {
    const node = document.createElement(tag);
    if (className) node.className = className;
    if (text !== undefined) node.textContent = text;
    return node;
}

export function icon(name) {
    const node = element('i');
    node.dataset.lucide = name;
    node.setAttribute('aria-hidden', 'true');
    return node;
}

export function refreshIcons() {
    window.lucide?.createIcons();
}

export function button(label, { style = '', glyph, action } = {}) {
    const node = element('button', ['btn', style].filter(Boolean).join(' '));
    node.type = 'button';
    if (glyph) node.append(icon(glyph), ' ');
    node.append(label);
    if (action) node.addEventListener('click', action);
    return node;
}

export function badge(text, tone = '') {
    return element('span', tone ? 'badge badge-' + tone : 'badge', text);
}

export function detail(text, secondary) {
    const wrapper = element('div', '', text);
    wrapper.append(element('span', 'cell-detail', secondary));
    return wrapper;
}

export function mountPage() {
    const identity = session();
    if (!identity) {
        location.replace('/');
        return null;
    }

    const current = document.body.dataset.page;
    const matching = GROUPS.flatMap(([, links]) => links).find(([slug]) => slug === current);
    if (!matching?.[3].includes(identity.role)) {
        location.replace(home(identity.role));
        return null;
    }

    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('nav-overlay');
    const topbar = document.getElementById('topbar');
    const mainArea = topbar.parentElement;
    const brand = element('a', 'brand');
    brand.href = home(identity.role);
    const mark = element('span', 'brand-mark');
    mark.append(icon('library-big'));
    brand.append(mark, 'Library Desk');
    sidebar.append(brand);

    const nav = element('nav');
    nav.setAttribute('aria-label', 'Library navigation');
    for (const [group, links] of GROUPS) {
        const allowed = links.filter(([, , , roles]) => roles.includes(identity.role));
        if (!allowed.length) continue;
        const section = element('section', 'nav-group');
        section.append(element('h2', '', group));
        for (const [slug, label, glyph] of allowed) {
            const link = element('a');
            link.href = '/' + slug + '.html';
            link.append(icon(glyph), label);
            if (slug === current) link.setAttribute('aria-current', 'page');
            section.append(link);
        }
        nav.append(section);
    }
    sidebar.append(nav);

    const footer = element('div', 'sidebar-footer');
    const logout = button('Sign out', { glyph: 'log-out', action: () => signOut() });
    logout.className = 'nav-button';
    footer.append(logout);
    sidebar.append(footer);

    const toggle = button('Menu', { style: 'btn-secondary btn-small menu-toggle', glyph: 'menu' });
    toggle.setAttribute('aria-expanded', 'false');
    toggle.setAttribute('aria-controls', 'sidebar');
    function setMenu(open, restoreFocus = true) {
        sidebar.classList.toggle('open', open);
        overlay.hidden = !open;
        mainArea.inert = open;
        toggle.setAttribute('aria-expanded', String(open));
        if (open) sidebar.querySelector('a').focus();
        else if (restoreFocus) toggle.focus();
    }
    toggle.addEventListener('click', () => setMenu(!sidebar.classList.contains('open')));
    overlay.addEventListener('click', () => setMenu(false));
    const mobile = matchMedia('(max-width: 760px)');
    mobile.addEventListener('change', (event) => {
        if (!event.matches) setMenu(false, false);
    });
    document.addEventListener('keydown', (event) => {
        if (event.key === 'Escape' && sidebar.classList.contains('open')) setMenu(false);
    });

    const left = element('div', 'topbar-left');
    left.append(toggle, element('span', '', identity.role === 'STUDENT' ? 'Student portal' : 'Library workspace'));
    const profile = element('div', 'profile');
    const profileText = detail(identity.username, { ADMIN: 'Administrator', LIBRARIAN: 'Librarian', STUDENT: 'Student' }[identity.role]);
    profileText.lastChild.className = 'small';
    profile.append(element('span', 'avatar', identity.username.slice(0, 1).toUpperCase()), profileText);
    topbar.append(left, profile);
    document.getElementById('app').hidden = false;
    refreshIcons();
    return identity;
}

export function pageAction(node) {
    const area = document.getElementById('page-actions');
    area.hidden = false;
    area.append(node);
    refreshIcons();
}

export function toast(message, isError = false) {
    let stack = document.querySelector('.toast-stack');
    if (!stack) {
        stack = element('div', 'toast-stack');
        document.body.append(stack);
    }
    const item = element('div', isError ? 'toast error' : 'toast');
    item.setAttribute('role', isError ? 'alert' : 'status');
    item.append(icon(isError ? 'circle-alert' : 'circle-check'), message);
    stack.append(item);
    refreshIcons();
    setTimeout(() => item.remove(), 5000);
}

const dateFormat = new Intl.DateTimeFormat('en-IN', { dateStyle: 'medium', timeZone: 'Asia/Kolkata' });
const currency = new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' });
export const date = (value) => value ? dateFormat.format(new Date(value.length === 10 ? value + 'T00:00:00+05:30' : value)) : '—';
export const money = (value) => currency.format(value ?? 0);

export function setOptions(select, list, label, describe) {
    const previous = select.value;
    select.replaceChildren(new Option(label, ''));
    for (const item of list) select.add(new Option(describe(item), item.id));
    if (list.some((item) => String(item.id) === previous)) select.value = previous;
}

export const bookOption = (book) => book.title + ' · ' + book.isbn;
export const studentOption = (student) => student.name + ' · ' + student.rollNumber;

export function showFormErrors(form, message, error) {
    message.textContent = error?.message ?? '';
    form.querySelectorAll('.field-error').forEach((node) => { node.textContent = ''; });
    form.querySelectorAll('[aria-invalid]').forEach((node) => node.removeAttribute('aria-invalid'));
    for (const [name, text] of Object.entries(error?.fields ?? {})) {
        const input = form.elements.namedItem(name);
        if (!(input instanceof HTMLElement)) continue;
        input.setAttribute('aria-invalid', 'true');
        let feedback = input.parentElement.querySelector('.field-error');
        if (!feedback) {
            feedback = element('span', 'field-error');
            feedback.id = (form.id || 'dialog-form') + '-' + name + '-error';
            input.parentElement.append(feedback);
            input.setAttribute('aria-describedby', feedback.id);
        }
        feedback.textContent = text;
    }
}
