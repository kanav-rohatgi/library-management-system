package com.example.library;

import com.example.library.entity.Book;
import com.example.library.entity.LoanTransaction;
import com.example.library.entity.Student;
import com.example.library.entity.StudentStatus;
import com.example.library.repository.BookRepository;
import com.example.library.repository.LoanTransactionRepository;
import com.example.library.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LibraryRepositoryTest {
    @Autowired private BookRepository books;
    @Autowired private StudentRepository students;
    @Autowired private LoanTransactionRepository loans;

    @Test
    void isbnIsUniqueInTheDatabase() {
        book("9780134685991");
        assertThatThrownBy(() -> book("9780134685991")).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rollNumberIsUniqueInTheDatabase() {
        student("CS001");
        assertThatThrownBy(() -> student("CS001")).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void scopesHistoryAndFinesAndRetainsHistoryAfterArchive() {
        LocalDate today = LocalDate.of(2026, 3, 20);
        Student firstStudent = student("CS001");
        Student secondStudent = student("CS002");
        Book firstBook = book("9780134685991");
        Book secondBook = book("9781449373320");

        var overdue = loans.save(new LoanTransaction(firstBook, firstStudent, today.minusDays(18),
                today.minusDays(4), null, BigDecimal.ZERO));
        var onTime = loans.save(new LoanTransaction(secondBook, firstStudent, today.minusDays(14),
                today, null, BigDecimal.ZERO));
        var returned = loans.save(new LoanTransaction(secondBook, secondStudent, today.minusDays(18),
                today.minusDays(4), today.minusDays(2), new BigDecimal("10.00")));
        var page = PageRequest.of(0, 10);

        assertThat(loans.history(firstStudent.getId(), null, true, page).getContent())
                .extracting(LoanTransaction::getId).containsExactlyInAnyOrder(overdue.getId(), onTime.getId());
        assertThat(loans.history(null, secondBook.getId(), false, page).getTotalElements()).isEqualTo(2);
        assertThat(loans.findByReturnDateIsNullAndDueDateBefore(today, page).getContent())
                .extracting(LoanTransaction::getId).containsExactly(overdue.getId());
        assertThat(loans.fines(null, today, page).getContent())
                .extracting(LoanTransaction::getId).containsExactlyInAnyOrder(overdue.getId(), returned.getId());
        assertThat(loans.fines(secondStudent.getId(), today, page).getContent())
                .extracting(LoanTransaction::getId).containsExactly(returned.getId());
        assertThat(loans.totalFines()).isEqualByComparingTo("10.00");

        secondStudent.archive();
        students.flush();
        assertThat(students.findByIdAndArchivedFalse(secondStudent.getId())).isEmpty();
        assertThat(loans.history(secondStudent.getId(), null, false, page).getTotalElements()).isOne();
    }

    private Book book(String isbn) {
        return books.saveAndFlush(new Book("Book " + isbn, "Author", isbn, "Programming", "Press", 2020, 2, 2));
    }

    private Student student(String rollNumber) {
        return students.saveAndFlush(new Student(rollNumber, "Asha Rao", "asha@example.test", "9000000011",
                "Computer Science", 2, StudentStatus.ACTIVE));
    }
}
