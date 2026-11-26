package com.example.library.config;

import com.example.library.entity.Book;
import com.example.library.entity.LibraryUser;
import com.example.library.entity.Role;
import com.example.library.entity.Student;
import com.example.library.entity.StudentStatus;
import com.example.library.repository.BookRepository;
import com.example.library.repository.LibraryUserRepository;
import com.example.library.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@ConditionalOnProperty(name = "library.seed.enabled", havingValue = "true")
public class DemoDataSeeder implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final BookRepository books;
    private final StudentRepository students;
    private final LibraryUserRepository users;
    private final PasswordEncoder encoder;
    private final String adminPassword;
    private final String librarianPassword;
    private final String studentPassword;

    public DemoDataSeeder(
            BookRepository books,
            StudentRepository students,
            LibraryUserRepository users,
            PasswordEncoder encoder,
            @Value("${library.seed.admin-password}") String adminPassword,
            @Value("${library.seed.librarian-password}") String librarianPassword,
            @Value("${library.seed.student-password}") String studentPassword) {
        this.books = books;
        this.students = students;
        this.users = users;
        this.encoder = encoder;
        this.adminPassword = adminPassword;
        this.librarianPassword = librarianPassword;
        this.studentPassword = studentPassword;
    }

    /**
     * The admin account marks completion of the initial seed. All demo records commit together.
     */
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (users.findByUsername("admin").isPresent()) {
            return;
        }

        Student student = students.findByRollNumber("CS001").orElseGet(() -> students.save(
                new Student("CS001", "Aarav Sharma", "aarav@example.test", "9000000001",
                        "Computer Science", 2, StudentStatus.ACTIVE)));

        for (Book book : List.of(
                new Book("Effective Java", "Joshua Bloch", "9780134685991",
                        "Programming", "Addison-Wesley", 2018, 4, 4),
                new Book("Designing Data-Intensive Applications", "Martin Kleppmann", "9781449373320",
                        "Databases", "O'Reilly Media", 2017, 3, 3),
                new Book("Computer Networking: A Top-Down Approach", "James Kurose and Keith Ross", "9780136681557",
                        "Networking", "Pearson", 2021, 2, 2))) {
            if (books.findByIsbn(book.getIsbn()).isEmpty()) {
                books.save(book);
            }
        }

        users.saveAll(List.of(
                new LibraryUser("librarian", encoder.encode(librarianPassword), Role.LIBRARIAN, null),
                new LibraryUser("student", encoder.encode(studentPassword), Role.STUDENT, student),
                new LibraryUser("admin", encoder.encode(adminPassword), Role.ADMIN, null)));
        LOGGER.info("Initialized library demo data");
    }
}
