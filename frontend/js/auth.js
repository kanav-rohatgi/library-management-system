const SESSION_KEY = 'library-desk.session';
const ROLES = new Set(['ADMIN', 'LIBRARIAN', 'STUDENT']);

export function session() {
    try {
        const value = JSON.parse(localStorage.getItem(SESSION_KEY));
        if (value && ROLES.has(value.role) && typeof value.username === 'string'
                && typeof value.token === 'string' && value.token) {
            return value;
        }
    } catch {
        return null;
    }
    return null;
}

export function signIn(identity) {
    localStorage.setItem(SESSION_KEY, JSON.stringify(identity));
}

export function signOut({ expired = false } = {}) {
    localStorage.removeItem(SESSION_KEY);
    location.replace(expired ? '/?expired=1' : '/');
}

export function home(role) {
    return role === 'STUDENT' ? '/my-books.html' : '/dashboard.html';
}
