package com.example.library;

import com.example.library.entity.LibraryUser;
import com.example.library.entity.Role;
import com.example.library.repository.BookRepository;
import com.example.library.repository.LibraryUserRepository;
import com.example.library.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "library.seed.enabled=true")
@ActiveProfiles("test")
class LibraryApplicationTest {
    @Autowired
    private BookRepository books;
    @Autowired
    private StudentRepository students;
    @Autowired
    private LibraryUserRepository users;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void startsWithSeededBooksStudentsAndRoleAccounts() {
        assertThat(books.findByIsbn("9780134685991")).isPresent();
        assertThat(students.findByRollNumber("CS001")).isPresent();
        assertThat(users.findAll()).extracting(LibraryUser::getRole)
                .containsExactlyInAnyOrder(Role.ADMIN, Role.LIBRARIAN, Role.STUDENT);
        LibraryUser admin = users.findByUsername("admin").orElseThrow();
        assertThat(passwordEncoder.matches("DemoAdmin123!", admin.getPasswordHash())).isTrue();
    }
}
