package com.example.library;

import com.example.library.dto.CirculationDto.IssueInput;
import com.example.library.dto.CirculationDto.LoanView;
import com.example.library.entity.Book;
import com.example.library.entity.LoanTransaction;
import com.example.library.entity.Student;
import com.example.library.entity.StudentStatus;
import com.example.library.repository.BookRepository;
import com.example.library.repository.LoanTransactionRepository;
import com.example.library.repository.ReservationRepository;
import com.example.library.repository.StudentRepository;
import com.example.library.service.LibraryException;
import com.example.library.service.impl.CirculationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CirculationServiceTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-03-20T10:00:00Z"), ZoneOffset.UTC);
    private static final LocalDate TODAY = LocalDate.now(CLOCK);
    private static final long BOOK_ID = 11L;
    private static final long STUDENT_ID = 22L;
    private static final long LOAN_ID = 33L;

    @Mock private BookRepository books;
    @Mock private StudentRepository students;
    @Mock private LoanTransactionRepository loans;
    @Mock private ReservationRepository reservations;

    private CirculationServiceImpl service;
    private Student student;

    @BeforeEach
    void setUp() {
        service = new CirculationServiceImpl(books, students, loans, reservations, CLOCK);
        student = new Student("CS001", "Asha Rao", "asha@example.test", "9000000011",
                "Computer Science", 2, StudentStatus.ACTIVE);
        ReflectionTestUtils.setField(student, "id", STUDENT_ID);
    }

    @Test
    void issueTakesOneCopyAndUsesAFourteenDayLoanPeriod() {
        Book book = bookWithCopies(1);
        lockInventory(book);
        when(loans.save(any(LoanTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoanView loan = service.issue(new IssueInput(BOOK_ID, STUDENT_ID));

        assertThat(book.getAvailableCopies()).isZero();
        assertThat(loan.issueDate()).isEqualTo(TODAY);
        assertThat(loan.dueDate()).isEqualTo(TODAY.plusDays(14));
        assertThat(loan.returnDate()).isNull();
    }

    @Test
    void rejectsIssueWhenNoCopiesRemain() {
        Book book = bookWithCopies(0);
        lockInventory(book);

        assertIssueConflict("No copies are available");
        assertThat(book.getAvailableCopies()).isZero();
    }

    @Test
    void rejectsFourthActiveLoan() {
        Book book = bookWithCopies(1);
        lockInventory(book);
        when(loans.countByStudentIdAndReturnDateIsNull(STUDENT_ID)).thenReturn(3L);

        assertIssueConflict("three active loans");
        assertThat(book.getAvailableCopies()).isOne();
    }

    @Test
    void rejectsDuplicateActiveLoan() {
        Book book = bookWithCopies(1);
        lockInventory(book);
        when(loans.existsByStudentIdAndBookIdAndReturnDateIsNull(STUDENT_ID, BOOK_ID)).thenReturn(true);

        assertIssueConflict("already has an active loan");
        assertThat(book.getAvailableCopies()).isOne();
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 3})
    void returnRestoresInventoryAndChargesOnlyForLateDays(int lateDays) {
        Book book = bookWithCopies(0);
        lockInventory(book);
        LocalDate due = TODAY.minusDays(lateDays);
        LoanTransaction loan = new LoanTransaction(book, student, due.minusDays(14), due, null, BigDecimal.ZERO);
        when(loans.lockLoan(LOAN_ID)).thenReturn(Optional.of(loan));

        LoanView result = service.returnLoan(LOAN_ID);

        assertThat(result.returnDate()).isEqualTo(TODAY);
        assertThat(result.fineAmount()).isEqualByComparingTo(BigDecimal.valueOf(Math.max(0, lateDays) * 5L));
        assertThat(book.getAvailableCopies()).isOne();
    }

    @Test
    void repeatedReturnDoesNotRestoreAnotherCopy() {
        Book book = bookWithCopies(1);
        LoanTransaction loan = new LoanTransaction(book, student, TODAY.minusDays(14), TODAY, TODAY, BigDecimal.ZERO);
        when(loans.lockLoan(LOAN_ID)).thenReturn(Optional.of(loan));

        assertThatExceptionOfType(LibraryException.class).isThrownBy(() -> service.returnLoan(LOAN_ID))
                .withMessageContaining("already been returned");
        assertThat(book.getAvailableCopies()).isOne();
        verify(books, never()).lockActive(BOOK_ID);
    }

    private void assertIssueConflict(String message) {
        assertThatExceptionOfType(LibraryException.class)
                .isThrownBy(() -> service.issue(new IssueInput(BOOK_ID, STUDENT_ID)))
                .withMessageContaining(message)
                .satisfies(error -> assertThat(error.getStatus()).isEqualTo(HttpStatus.CONFLICT));
        verify(loans, never()).save(any(LoanTransaction.class));
    }

    private Book bookWithCopies(int available) {
        Book book = new Book("Effective Java", "Joshua Bloch", "9780134685991",
                "Programming", "Addison-Wesley", 2018, 1, available);
        ReflectionTestUtils.setField(book, "id", BOOK_ID);
        return book;
    }

    private void lockInventory(Book book) {
        when(books.lockActive(BOOK_ID)).thenReturn(Optional.of(book));
        when(students.lockActive(STUDENT_ID)).thenReturn(Optional.of(student));
    }
}
