import { api } from './api.js';
import { openForm } from './dialog.js';
import { dataTable } from './table.js';
import { badge, button, detail, element, mountPage, pageAction, toast } from './ui.js';

const fields = [
    { name: 'rollNumber', label: 'Roll number', maxLength: 40 },
    { name: 'name', label: 'Full name', maxLength: 160 },
    { name: 'email', label: 'Email address', type: 'email', maxLength: 254 },
    { name: 'phone', label: 'Phone number', type: 'tel', maxLength: 32 },
    { name: 'department', label: 'Department', maxLength: 120 },
    { name: 'academicYear', label: 'Academic year', type: 'number', min: 1 },
    { name: 'status', label: 'Status', options: { ACTIVE: 'Active', INACTIVE: 'Inactive' } },
];

function run() {
    const table = dataTable('students-table', {
        empty: 'No students are registered.',
        columns: [
            ['Student', (student) => detail(student.name, student.rollNumber)],
            ['Contact', (student) => detail(student.email, student.phone)],
            ['Department', (student) => student.department],
            ['Year', (student) => student.academicYear],
            ['Status', (student) => student.status === 'ACTIVE' ? badge('Active', 'good') : badge('Inactive')],
            ['Actions', actions],
        ],
        fetchPage: (params, signal) => api('/students', { params, signal }),
    });

    function edit(student) {
        openForm({
            title: student ? 'Edit student' : 'Add student',
            fields: fields.map((field) => ({ ...field, value: student?.[field.name] })),
            submitLabel: student ? 'Save changes' : 'Add student',
            save: async (values) => {
                await api(student ? '/students/' + student.id : '/students', { method: student ? 'PUT' : 'POST', body: values });
                toast(student ? 'Student updated.' : 'Student added.');
                table.load(student ? undefined : 0);
            },
        });
    }

    function actions(student) {
        const group = element('div', 'button-group');
        group.append(
            button('Edit', { style: 'btn-secondary btn-small', action: () => edit(student) }),
            button('Create login', {
                style: 'btn-secondary btn-small',
                action: () => openForm({
                    title: 'Create student login', description: student.name, submitLabel: 'Create login',
                    fields: [
                        { name: 'username', label: 'Username', maxLength: 80, pattern: '[A-Za-z0-9._\\-]+' },
                        { name: 'password', label: 'Password', type: 'password', minLength: 8, maxLength: 72 },
                    ],
                    save: async (body) => {
                        await api('/students/' + student.id + '/account', { method: 'POST', body });
                        toast('Student login created.');
                    },
                }),
            }),
            button('Delete', {
                style: 'btn-secondary btn-small',
                action: () => openForm({
                    title: 'Delete student', description: 'Remove ' + student.name + ' from the student list?',
                    submitLabel: 'Delete student', danger: true,
                    save: async () => {
                        await api('/students/' + student.id, { method: 'DELETE' });
                        toast('Student deleted.');
                        table.load();
                    },
                }),
            }),
        );
        return group;
    }

    pageAction(button('Add student', { glyph: 'plus', action: () => edit() }));
    table.load();
}

if (mountPage()) run();
