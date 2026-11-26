import { api } from './api.js';
import { home, session, signIn } from './auth.js';
import { refreshIcons, showFormErrors } from './ui.js';

const identity = session();
if (identity) {
    location.replace(home(identity.role));
} else {
    const form = document.getElementById('login-form');
    const submit = form.querySelector('button');
    const feedback = document.getElementById('login-error');
    if (new URLSearchParams(location.search).has('expired')) {
        feedback.textContent = 'Your session ended. Sign in to continue.';
    }
    refreshIcons();

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        showFormErrors(form, feedback, null);
        submit.disabled = true;
        submit.textContent = 'Signing in…';
        try {
            const fields = new FormData(form);
            const result = await api('/auth/login', {
                method: 'POST', authenticated: false,
                body: { username: fields.get('username').trim(), password: fields.get('password') },
            });
            signIn(result);
            location.replace(home(result.role));
        } catch (error) {
            showFormErrors(form, feedback, error);
        } finally {
            submit.disabled = false;
            submit.textContent = 'Sign in';
        }
    });
}
