package com.example.library.service.impl;

import com.example.library.dto.CirculationDto.DashboardView;
import com.example.library.dto.CirculationDto.IssueInput;
import com.example.library.dto.CirculationDto.LoanView;
import com.example.library.dto.CirculationDto.ReservationView;
import com.example.library.dto.PageView;
import com.example.library.entity.Book;
import com.example.library.entity.LoanTransaction;
import com.example.library.entity.Reservation;
import com.example.library.entity.ReservationStatus;
import com.example.library.entity.Student;
import com.example.library.entity.StudentStatus;
import com.example.library.repository.BookRepository;
import com.example.library.repository.LoanTransactionRepository;
import com.example.library.repository.ReservationRepository;
import com.example.library.repository.StudentRepository;
import com.example.library.service.CirculationService;
import com.example.library.service.LibraryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class CirculationServiceImpl implements CirculationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CirculationServiceImpl.class);
    private static final int MAX_ACTIVE_LOANS = 3;
    private static final int LOAN_DAYS = 14;
    private static final BigDecimal DAILY_FINE = new BigDecimal("5.00");
    private static final BigDecimal NO_FINE = new BigDecimal("0.00");

    private final BookRepository books;
    private final StudentRepository students;
    private final LoanTransactionRepository loans;
    private final ReservationRepository reservations;
    private final Clock clock;

    public CirculationServiceImpl(BookRepository books, StudentRepository students,
                                  LoanTransactionRepository loans, ReservationRepository reservations, Clock clock) {
        this.books = books;
        this.students = students;
        this.loans = loans;
        this.reservations = reservations;
        this.clock = clock;
    }

    /** Serialize issues for each student and inventory changes for each book. */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public LoanView issue(IssueInput input) {
        Student student = lockStudent(input.studentId());
        requireActive(student);
        Book book = lockBook(input.bookId());
        if (loans.existsByStudentIdAndBookIdAndReturnDateIsNull(student.getId(), book.getId())) {
            throw LibraryException.conflict("Student already has an active loan for this book");
        }
        if (loans.countByStudentIdAndReturnDateIsNull(student.getId()) >= MAX_ACTIVE_LOANS) {
            throw LibraryException.conflict("Student already has three active loans");
        }
        if (book.getAvailableCopies() == 0) {
            throw LibraryException.conflict("No copies are available");
        }

        LocalDate today = LocalDate.now(clock);
        book.issueCopy();
        LoanTransaction loan = loans.save(new LoanTransaction(
                book, student, today, today.plusDays(LOAN_DAYS), null, NO_FINE));
        reservations.findByStudentIdAndBookIdAndStatus(student.getId(), book.getId(), ReservationStatus.PENDING)
                .ifPresent(reservation -> reservations.transitionPending(reservation.getId(), ReservationStatus.FULFILLED));
        LOGGER.debug("Issued loan {} for student {} and book {}", loan.getId(), student.getId(), book.getId());
        return view(loan, today);
    }

    /** Lock the loan so a repeated return cannot increment inventory twice. */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public LoanView returnLoan(Long id) {
        LoanTransaction loan = loans.lockLoan(id).orElseThrow(() -> LibraryException.missing("Loan not found"));
        if (loan.getReturnDate() != null) {
            throw LibraryException.conflict("Loan has already been returned");
        }
        lockStudent(loan.getStudent().getId());
        Book book = lockBook(loan.getBook().getId());
        LocalDate today = LocalDate.now(clock);
        loan.complete(today, fine(loan.getDueDate(), today));
        book.returnCopy();
        LOGGER.debug("Returned loan {}", id);
        return view(loan, today);
    }

    @Override
    public PageView<LoanView> history(Long studentId, Long bookId, boolean activeOnly, Pageable pageable) {
        LocalDate today = LocalDate.now(clock);
        return PageView.from(loans.history(studentId, bookId, activeOnly, pageable).map(loan -> view(loan, today)));
    }

    @Override
    public PageView<LoanView> overdue(Pageable pageable) {
        LocalDate today = LocalDate.now(clock);
        return PageView.from(loans.findByReturnDateIsNullAndDueDateBefore(today, pageable)
                .map(loan -> view(loan, today)));
    }

    @Override
    public PageView<LoanView> fines(Long studentId, Pageable pageable) {
        LocalDate today = LocalDate.now(clock);
        return PageView.from(loans.fines(studentId, today, pageable).map(loan -> view(loan, today)));
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ReservationView reserve(Long studentId, Long bookId) {
        Student student = lockStudent(studentId);
        requireActive(student);
        Book book = lockBook(bookId);
        if (book.getAvailableCopies() > 0) {
            throw LibraryException.conflict("Reservations are only available for books with no available copies");
        }
        if (reservations.findByStudentIdAndBookIdAndStatus(studentId, bookId, ReservationStatus.PENDING).isPresent()) {
            throw LibraryException.conflict("Student already has a pending reservation for this book");
        }
        return ReservationView.from(reservations.save(
                new Reservation(book, student, clock.instant(), ReservationStatus.PENDING)));
    }

    @Override
    public PageView<ReservationView> reservations(Long studentId, Pageable pageable) {
        return PageView.from(reservations.list(studentId, pageable).map(ReservationView::from));
    }

    @Override
    @Transactional
    public void cancelReservation(Long id, Long studentId) {
        Reservation reservation = reservations.findById(id)
                .filter(item -> studentId == null || Objects.equals(item.getStudent().getId(), studentId))
                .orElseThrow(() -> LibraryException.missing("Reservation not found"));
        if (reservations.transitionPending(reservation.getId(), ReservationStatus.CANCELLED) == 0) {
            throw LibraryException.conflict("Only pending reservations can be cancelled");
        }
    }

    @Override
    public DashboardView dashboard() {
        Map<String, Long> categories = new LinkedHashMap<>();
        books.countByCategory().forEach(row -> categories.put(row.getCategory(), row.getTotal()));
        return new DashboardView(books.countByArchivedFalse(), students.countByArchivedFalse(),
                loans.countByReturnDateIsNull(), loans.countByReturnDateIsNullAndDueDateBefore(LocalDate.now(clock)),
                loans.countByReturnDateIsNotNull(), loans.totalFines(), categories);
    }

    private Student lockStudent(Long id) {
        return students.lockActive(id).orElseThrow(() -> LibraryException.missing("Student not found"));
    }

    private Book lockBook(Long id) {
        return books.lockActive(id).orElseThrow(() -> LibraryException.missing("Book not found"));
    }

    private void requireActive(Student student) {
        if (student.getStatus() != StudentStatus.ACTIVE) {
            throw LibraryException.conflict("Student account is inactive");
        }
    }

    private LoanView view(LoanTransaction loan, LocalDate today) {
        boolean overdue = loan.getReturnDate() == null && today.isAfter(loan.getDueDate());
        return LoanView.from(loan, overdue ? fine(loan.getDueDate(), today) : NO_FINE, overdue);
    }

    private BigDecimal fine(LocalDate dueDate, LocalDate endDate) {
        long lateDays = Math.max(0, ChronoUnit.DAYS.between(dueDate, endDate));
        return DAILY_FINE.multiply(BigDecimal.valueOf(lateDays));
    }
}
