import { session, signOut } from './auth.js';

export class ApiError extends Error {
    constructor(message, status, fields = {}) {
        super(message);
        this.status = status;
        this.fields = fields;
    }
}

export async function api(path, { method = 'GET', body, params, signal, authenticated = true } = {}) {
    const query = new URLSearchParams();
    for (const [key, value] of Object.entries(params ?? {})) {
        if (value !== '' && value !== null && value !== undefined) {
            query.set(key, value);
        }
    }

    const headers = { Accept: 'application/json' };
    const identity = authenticated ? session() : null;
    if (identity) headers.Authorization = 'Bearer ' + identity.token;
    if (body !== undefined) headers['Content-Type'] = 'application/json';

    let response;
    try {
        const suffix = query.size ? '?' + query.toString() : '';
        response = await fetch('/api' + path + suffix, {
            method, headers, signal, body: body === undefined ? undefined : JSON.stringify(body),
        });
    } catch (error) {
        if (error.name === 'AbortError') throw error;
        throw new ApiError('Unable to reach the library. Try again.', 0);
    }

    if (response.status === 401 && authenticated) {
        signOut({ expired: true });
        throw new ApiError('Please sign in again.', 401);
    }
    if (response.status === 204) return null;

    const payload = response.headers.get('Content-Type')?.includes('application/json')
        ? await response.json()
        : null;
    if (!response.ok) {
        throw new ApiError(payload?.message || 'The library could not complete this request.', response.status, payload?.errors);
    }
    return payload;
}

export async function allPages(path, params = {}) {
    const items = [];
    let page = 0;
    let result;
    do {
        result = await api(path, { params: { ...params, page, size: 100 } });
        items.push(...result.content);
        page++;
    } while (page < result.totalPages);
    return items;
}
