package com.example.library.service.impl;

import com.example.library.dto.CatalogDto.BookInput;
import com.example.library.dto.CatalogDto.BookView;
import com.example.library.dto.CatalogDto.StudentInput;
import com.example.library.dto.CatalogDto.StudentView;
import com.example.library.dto.PageView;
import com.example.library.entity.Book;
import com.example.library.entity.Student;
import com.example.library.repository.BookRepository;
import com.example.library.repository.LoanTransactionRepository;
import com.example.library.repository.ReservationRepository;
import com.example.library.repository.StudentRepository;
import com.example.library.service.CatalogService;
import com.example.library.service.LibraryException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class CatalogServiceImpl implements CatalogService {
    private final BookRepository books;
    private final StudentRepository students;
    private final LoanTransactionRepository loans;
    private final ReservationRepository reservations;

    public CatalogServiceImpl(BookRepository books, StudentRepository students,
                              LoanTransactionRepository loans, ReservationRepository reservations) {
        this.books = books;
        this.students = students;
        this.loans = loans;
        this.reservations = reservations;
    }

    @Override
    public PageView<BookView> books(String query, String category, Boolean available, Pageable pageable) {
        Specification<Book> search = (root, criteria, builder) -> builder.isFalse(root.get("archived"));
        if (query != null && !query.isBlank()) {
            String term = query.strip().toLowerCase(Locale.ROOT)
                    .replace("!", "!!").replace("%", "!%").replace("_", "!_");
            String pattern = "%" + term + "%";
            search = search.and((root, criteria, builder) -> builder.or(
                    builder.like(builder.lower(root.get("title")), pattern, '!'),
                    builder.like(builder.lower(root.get("author")), pattern, '!'),
                    builder.like(builder.lower(root.get("isbn")), pattern, '!')));
        }
        if (category != null && !category.isBlank()) {
            String value = category.strip().toLowerCase(Locale.ROOT);
            search = search.and((root, criteria, builder) ->
                    builder.equal(builder.lower(root.get("category")), value));
        }
        if (available != null) {
            search = search.and((root, criteria, builder) -> available
                    ? builder.gt(root.get("availableCopies"), 0)
                    : builder.equal(root.get("availableCopies"), 0));
        }
        return PageView.from(books.findAll(search, pageable).map(BookView::from));
    }

    @Override
    public BookView book(Long id) {
        return books.findByIdAndArchivedFalse(id).map(BookView::from)
                .orElseThrow(() -> LibraryException.missing("Book not found"));
    }

    @Override
    @Transactional
    public BookView createBook(BookInput input) {
        Book details = input.toEntity();
        checkIsbn(details.getIsbn(), null);
        return BookView.from(books.save(details));
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public BookView updateBook(Long id, BookInput input) {
        Book book = lockBook(id);
        Book details = input.toEntity();
        checkIsbn(details.getIsbn(), id);
        int checkedOut = book.getTotalCopies() - book.getAvailableCopies();
        if (details.getTotalCopies() < checkedOut) {
            throw LibraryException.conflict("Total copies cannot be lower than the number currently issued");
        }
        book.revise(details, checkedOut);
        return BookView.from(book);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteBook(Long id) {
        Book book = lockBook(id);
        if (loans.existsByBookIdAndReturnDateIsNull(id)) {
            throw LibraryException.conflict("Books with active loans cannot be deleted");
        }
        reservations.cancelPending(id, null);
        book.archive();
    }

    @Override
    public PageView<StudentView> students(Pageable pageable) {
        return PageView.from(students.findByArchivedFalse(pageable).map(StudentView::from));
    }

    @Override
    public StudentView student(Long id) {
        return students.findByIdAndArchivedFalse(id).map(StudentView::from)
                .orElseThrow(() -> LibraryException.missing("Student not found"));
    }

    @Override
    @Transactional
    public StudentView createStudent(StudentInput input) {
        Student details = input.toEntity();
        checkRollNumber(details.getRollNumber(), null);
        return StudentView.from(students.save(details));
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public StudentView updateStudent(Long id, StudentInput input) {
        Student student = lockStudent(id);
        Student details = input.toEntity();
        checkRollNumber(details.getRollNumber(), id);
        student.revise(details);
        return StudentView.from(student);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteStudent(Long id) {
        Student student = lockStudent(id);
        if (loans.countByStudentIdAndReturnDateIsNull(id) > 0) {
            throw LibraryException.conflict("Students with active loans cannot be deleted");
        }
        reservations.cancelPending(null, id);
        student.archive();
    }

    private Book lockBook(Long id) {
        return books.lockActive(id).orElseThrow(() -> LibraryException.missing("Book not found"));
    }

    private Student lockStudent(Long id) {
        return students.lockActive(id).orElseThrow(() -> LibraryException.missing("Student not found"));
    }

    private void checkIsbn(String isbn, Long id) {
        if (books.findByIsbn(isbn).filter(book -> !Objects.equals(book.getId(), id)).isPresent()) {
            throw LibraryException.conflict("ISBN is already registered");
        }
    }

    private void checkRollNumber(String rollNumber, Long id) {
        if (students.findByRollNumber(rollNumber).filter(student -> !Objects.equals(student.getId(), id)).isPresent()) {
            throw LibraryException.conflict("Roll number is already registered");
        }
    }
}
