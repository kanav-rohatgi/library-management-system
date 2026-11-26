# Test plan

Run `mvn test` from `backend/` with Java 17. The Maven suite uses H2 and does not require a Docker daemon. For the on-device demonstration, start the app from the repository root with `docker compose up --build` and use the demo logins in the README. Use a separate demo database for scenarios that modify data.

| Test case | Input | Expected result | Status |
| --- | --- | --- | --- |
| Seeded startup | Start with seeding enabled | Seed books, student record, and all three account roles exist. BCrypt verifies the demo admin password. | Pass · Maven |
| Authentication required | Request books without a header and with a malformed bearer token | Both return 401 in JSON. The unauthenticated response advertises the Bearer scheme. | Pass · Maven |
| Wrong password | Sign in with a valid username and wrong password | 401 with a generic message and no token | Pass · Maven |
| Staff boundary | Student token used on staff transaction or book deletion routes | 403 with JSON error | Pass · Maven |
| Administrator boundary | Librarian and administrator tokens used to list librarian accounts | Librarian gets 403. Administrator gets results without password hashes. | Pass · Maven |
| Request validation | Submit an empty book and request an unknown sort field | 400 with field errors for the empty book and an error naming the unsupported sort field | Pass · Maven |
| Unique identifiers | Insert duplicate ISBN and roll number records | Database rejects both writes | Pass · Maven |
| Book search | Search for Java and request a title sort | Filtered response contains Effective Java | Pass · Maven |
| Issue success | Eligible student and one available copy | Available count falls to zero, and the due date is 14 days after issue | Pass · Maven |
| No copies available | Issue an unavailable book | 409 and no new loan | Pass · Maven |
| Loan cap | Issue a fourth distinct book to the same active student | 409 and no new loan | Pass · Maven |
| Duplicate loan | Issue a book the student already holds | 409 and availability does not change | Pass · Maven |
| Return before or on the due date | Return one day early and on the due date | ₹0 fine and one copy restored in each scenario | Pass · Maven |
| Late return | Return three days after the due date | ₹15 fine and one copy restored | Pass · Maven |
| Repeated return | Return an already completed loan | 409 and no extra copy restored | Pass · Maven |
| Queries and archived records | Mix on-time, overdue, and returned loans across two students and books, then archive a student | History and reports return the correct rows and recorded fine total. The archived student's history stays available. | Pass · Maven |
| Student privacy | Request own history and fines using two different student accounts | Each student sees only their own records | Pass · MySQL API |
| Last-copy race | Two staff requests simultaneously issue the same last copy to two students | One 201 and one 409 | Pass · MySQL API |
| Protected deletion | Delete a book or student with an open loan | 409 until the loan is returned | Pass · MySQL API |
| Reservation state | Reserve an unavailable book, attempt a duplicate, then issue the book to the reserving student | A single pending reservation is created. Duplicate gets 409. Issue marks it fulfilled. | Pass · MySQL API |
| Dashboard and catalogue | Sign in as administrator, add and search for a book, issue and return it | Dashboard charts render, catalogue updates, and the active loan disappears after return | Pass · Chromium with MySQL |
| Mobile student flow | Sign in at a 390px viewport, open the menu, reserve and cancel a book, and visit a staff-only URL | Student menu is shown, own loan is visible, reservation is cancelled, and the staff URL redirects to My books | Pass · Chromium with MySQL |
| Frontend image | Build the Nginx image, request all pages and assets, and proxy a request to a test upstream | Static requests and the proxied route return 200 | Pass · Docker |
| Fresh Compose stack | Build both images and start a fresh stack with the default `.env` | Backend health, Swagger UI, Adminer, frontend sign-in, and authenticated catalogue through Nginx all respond | GitHub Actions check · see latest run |
