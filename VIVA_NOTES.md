# Viva notes

## Architecture and data model

The project separates a buildless frontend from a Java 17 Spring Boot backend. Nginx serves the HTML, CSS, and JavaScript and forwards `/api/` to Spring Boot. The controller validates input and returns response DTOs. Services apply authorization-sensitive business rules and own transaction boundaries. Repositories use JPA and Hibernate to read and write MySQL.

| Entity | Purpose |
| --- | --- |
| Book | Catalogue metadata, unique ISBN, and total and available copy counts |
| Student | Unique roll number, contact details, department, academic year, and active or inactive status |
| Loan transaction | Links one book and student and records issue, due, and return dates plus the recorded fine |
| Reservation | Links a student to an unavailable book and tracks pending, fulfilled, or cancelled status |
| Library user | Stores a unique username, BCrypt password hash, role, and optional student link. A role is an enum. |

## Frontend and JWT flow

The sign-in page calls `POST /api/auth/login`. The backend looks up the account, compares the password with its BCrypt hash, and issues a signed token containing the account ID and role. The frontend saves the session in local storage, and the shared `api.js` helper adds the bearer token to subsequent requests. Spring Security checks signature, issuer, and expiration, then loads the current account. The effective role and student identity come from that account. Method rules enforce staff, admin, and student access. Student history and reservations are scoped to the authenticated student, and a 401 sends the user back to sign-in.

## Issue and return flow

1. Staff select a student and a book. The form sends their IDs to `POST /api/transactions/issue`.
2. The controller validates both IDs. In one transaction, the service locks the student and book rows.
3. The service checks active student status, duplicate active loans, the three-loan limit, and copy availability.
4. It decrements available copies and saves a loan due 14 days after issue. If the student had a pending reservation for that book, it marks the reservation fulfilled.
5. On return the service locks the loan, rejects repeated returns, calculates the fine, updates the return record, and restores one available copy.

A fine is `max(0, days between due date and return date) × ₹5`. Returning on or before the due date costs ₹0. A return three days late costs ₹15. The reports display accrued fines for open overdue loans and recorded fines for completed loans. Library dates default to Asia/Kolkata and can be configured.

## Docker and verification

The backend Dockerfile uses a Maven build stage and a Java 17 runtime stage that launches the packaged jar as a non-root user. The frontend image runs Nginx. Compose starts MySQL and uses a health check before it starts the backend. The frontend waits for backend health. The service names `db` and `backend` resolve through Compose DNS, and a named volume persists MySQL data. Adminer is available on port 8081. Hibernate creates tables automatically and the startup runner seeds the local demo.

JUnit and Mockito verify circulation rules with a fixed clock. JPA tests verify constraints and report queries against H2. MockMvc tests send real JWTs through the security filter and check status codes, roles, response fields, and validation.

## Demonstration sequence

1. Start Compose with `docker compose up --build` and open the sign-in page.
2. Sign in as librarian. Show the dashboard, search the book list, and add a book with one copy.
3. Issue that book to the seeded student. Show the due date and the catalogue changing to unavailable. Attempt a duplicate issue through Swagger to show the conflict response.
4. Sign in as student and show **My books** and the role-specific navigation. Try to open a staff URL and show the redirect. To demonstrate a reservation, choose a different unavailable book or use a second student account.
5. Return the first book as librarian and show the restored copy and completed transaction history. Describe how the test suite verifies late fees without waiting for a real due date.
6. Sign in as administrator and show librarian account management. Open Swagger, the Actuator health endpoint, and Adminer to show the service boundaries.

## 25 viva questions and answers

### Design and implementation

1. **What problem does the project solve?** It centralizes catalogue, student, circulation, and reservation records so staff can track availability and overdue loans.
2. **Why separate the frontend and backend?** The frontend handles presentation, while the API centralizes permissions, validation, and persistent business rules.
3. **What does each application layer do?** Controllers handle HTTP, services enforce library rules and transactions, and repositories query the database.
4. **Why use DTOs?** They define the public API fields, support validation, and prevent fields such as password hashes from being serialized.
5. **Why is the loan its own entity?** Each issue has its own dates and fine, and a book or student can appear in many transactions over time.
6. **How does MySQL prevent duplicate books and students?** The ISBN and roll number columns have unique constraints, including when two requests race.
7. **How do two requests competing for the last copy behave?** A book row lock serializes inventory checks. After the first issue commits, the next request sees the changed availability.
8. **How is the three-loan limit protected?** Issuing locks the student record before counting active loans, so requests for that student cannot both pass the limit check concurrently.
9. **How is a late fine calculated?** Multiply positive calendar days after the due date by ₹5. A return on the due date has a zero fine.
10. **What happens when staff delete a book with active loans?** The API returns 409. After the active loans are resolved, staff can archive it and keep its completed history.
11. **When is a reservation fulfilled?** The circulation service marks the matching pending reservation fulfilled when that student receives the book.
12. **How does search support large lists?** The database filters eligible rows and returns the requested page using an allowed sort field.
13. **How is a return protected against repetition?** The transaction locks the loan, checks whether it already has a return date, and rejects the repeat before changing inventory.
14. **Why use a fixed clock in unit tests?** It makes due dates and late-fee edge cases repeatable regardless of when tests run.
15. **What does H2 verify?** It runs the JPA and controller tests without an external database. The Compose smoke check exercises the MySQL deployment.

### REST and JWT

16. **What does stateless authentication mean here?** Each protected request presents its bearer token, and the backend does not need a server-side sign-in session.
17. **How is a JWT checked?** Spring Security verifies its signature, issuer, and expiry, then loads the account to check it is still allowed to sign in.
18. **What is the difference between 401 and 403?** A 401 means authentication is required or failed. A 403 means the authenticated role lacks permission.
19. **Why hash passwords with BCrypt?** It stores salted, one-way hashes. The server can check a submitted password without storing the original password.
20. **How does the student API prevent seeing someone else's loans?** It passes the authenticated account's student ID to the repository and applies that filter to history and fines.

### Docker

21. **What is the difference between an image and a container?** An image is a reusable package. A container is a running or stopped instance of that package.
22. **Why use a multi-stage backend build?** The build stage compiles and packages the jar. The runtime stage carries the jar and JRE, keeping Maven out of the final image.
23. **How does the backend find MySQL?** Compose provides internal DNS for the `db` service, which the Docker profile uses as its datasource host.
24. **Why are health checks used with service dependencies?** The backend waits for a database query to succeed, and Nginx waits for the backend health endpoint to succeed.
25. **What is the difference between `docker compose down` and `docker compose down -v`?** Both stop the services. The `-v` option also removes the named database volume and its data.
