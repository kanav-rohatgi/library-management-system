-- MySQL 8 reference schema. Hibernate manages the running application's schema.
CREATE TABLE books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(160) NOT NULL,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    category VARCHAR(80) NOT NULL,
    publisher VARCHAR(160) NOT NULL,
    publication_year INT NOT NULL,
    total_copies INT NOT NULL,
    available_copies INT NOT NULL,
    archived BOOLEAN NOT NULL DEFAULT FALSE,
    CHECK (total_copies >= 0 AND available_copies BETWEEN 0 AND total_copies),
    INDEX ix_books_title (title),
    INDEX ix_books_author (author),
    INDEX ix_books_category (category)
);

CREATE TABLE students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    roll_number VARCHAR(40) NOT NULL UNIQUE,
    name VARCHAR(160) NOT NULL,
    email VARCHAR(254) NOT NULL,
    phone VARCHAR(32) NOT NULL,
    department VARCHAR(120) NOT NULL,
    academic_year INT NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL,
    archived BOOLEAN NOT NULL DEFAULT FALSE,
    CHECK (academic_year >= 1)
);

CREATE TABLE loan_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE,
    fine_amount DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (book_id) REFERENCES books(id),
    FOREIGN KEY (student_id) REFERENCES students(id),
    CHECK (due_date >= issue_date AND (return_date IS NULL OR return_date >= issue_date) AND fine_amount >= 0),
    INDEX ix_loan_transactions_student_return_date (student_id, return_date),
    INDEX ix_loan_transactions_book_return_date (book_id, return_date),
    INDEX ix_loan_transactions_return_date_due_date (return_date, due_date)
);

CREATE TABLE reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    reserved_at DATETIME(6) NOT NULL,
    status ENUM('CANCELLED', 'FULFILLED', 'PENDING') NOT NULL,
    FOREIGN KEY (book_id) REFERENCES books(id),
    FOREIGN KEY (student_id) REFERENCES students(id),
    INDEX ix_reservations_student_status (student_id, status),
    INDEX ix_reservations_book_status_reserved_at (book_id, status, reserved_at)
);

CREATE TABLE library_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(80) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role ENUM('ADMIN', 'LIBRARIAN', 'STUDENT') NOT NULL,
    student_id BIGINT UNIQUE,
    FOREIGN KEY (student_id) REFERENCES students(id),
    CHECK ((role = 'STUDENT' AND student_id IS NOT NULL) OR (role <> 'STUDENT' AND student_id IS NULL))
);
