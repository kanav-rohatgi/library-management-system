import { button, element, refreshIcons, showFormErrors } from './ui.js';

export function openForm({ title, description, fields = [], submitLabel, danger = false, save }) {
    const dialog = element('dialog');
    const heading = element('h2', '', title);
    heading.id = 'dialog-heading';
    dialog.setAttribute('aria-labelledby', heading.id);
    const header = element('div', 'panel-header');
    header.append(heading);
    dialog.append(header);
    if (description) dialog.append(element('p', 'muted', description));
    const form = element('form');
    const inputs = element('div', 'form-grid');
    const errorMessage = element('p', 'error-text');
    errorMessage.setAttribute('role', 'alert');

    for (const field of fields) {
        const label = element('label', '', field.label);
        const input = element(field.options ? 'select' : 'input');
        input.name = field.name;
        input.required = true;
        if (field.options) {
            for (const [value, text] of Object.entries(field.options)) input.add(new Option(text, value));
        } else {
            input.type = field.type ?? 'text';
            if (field.min !== undefined) input.min = field.min;
            if (field.minLength !== undefined) input.minLength = field.minLength;
            if (field.maxLength !== undefined) input.maxLength = field.maxLength;
            if (field.pattern) input.pattern = field.pattern;
            if (field.type === 'password') input.autocomplete = 'new-password';
        }
        if (field.value !== undefined) input.value = field.value;
        label.append(input);
        inputs.append(label);
    }

    const actions = element('div', 'form-footer');
    const cancel = button('Cancel', { style: 'btn-secondary', action: () => dialog.close() });
    const submit = button(submitLabel, { style: danger ? 'btn-danger' : '' });
    submit.type = 'submit';
    actions.append(cancel, submit);
    form.append(inputs, errorMessage, actions);
    dialog.append(form);
    document.body.append(dialog);

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        showFormErrors(form, errorMessage, null);
        const data = Object.fromEntries(new FormData(form));
        for (const field of fields) {
            if (field.type === 'number') data[field.name] = Number(data[field.name]);
            else if (field.type !== 'password') data[field.name] = data[field.name].trim();
        }
        submit.disabled = cancel.disabled = true;
        submit.textContent = 'Saving…';
        try {
            await save(data);
            dialog.close();
        } catch (error) {
            showFormErrors(form, errorMessage, error);
        } finally {
            submit.disabled = cancel.disabled = false;
            submit.textContent = submitLabel;
        }
    });
    dialog.addEventListener('cancel', (event) => {
        if (submit.disabled) event.preventDefault();
    });
    dialog.addEventListener('close', () => dialog.remove());
    dialog.showModal();
    refreshIcons();
}
