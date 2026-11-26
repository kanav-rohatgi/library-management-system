package com.example.library.repository;

import com.example.library.entity.LoanTransaction;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface LoanTransactionRepository extends JpaRepository<LoanTransaction, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from LoanTransaction l where l.id = :id")
    Optional<LoanTransaction> lockLoan(Long id);

    boolean existsByBookIdAndReturnDateIsNull(Long bookId);

    long countByStudentIdAndReturnDateIsNull(Long studentId);

    boolean existsByStudentIdAndBookIdAndReturnDateIsNull(Long studentId, Long bookId);

    long countByReturnDateIsNull();

    long countByReturnDateIsNotNull();

    long countByReturnDateIsNullAndDueDateBefore(LocalDate date);

    @Query("select coalesce(sum(l.fineAmount), 0) from LoanTransaction l")
    BigDecimal totalFines();

    @EntityGraph(attributePaths = {"book", "student"})
    @Query("""
            select l from LoanTransaction l
            where (:studentId is null or l.student.id = :studentId)
              and (:bookId is null or l.book.id = :bookId)
              and (:activeOnly = false or l.returnDate is null)
            """)
    Page<LoanTransaction> history(Long studentId, Long bookId, boolean activeOnly, Pageable pageable);

    @EntityGraph(attributePaths = {"book", "student"})
    Page<LoanTransaction> findByReturnDateIsNullAndDueDateBefore(LocalDate date, Pageable pageable);

    @EntityGraph(attributePaths = {"book", "student"})
    @Query("""
            select l from LoanTransaction l
            where (:studentId is null or l.student.id = :studentId)
              and (l.fineAmount > 0 or (l.returnDate is null and l.dueDate < :date))
            """)
    Page<LoanTransaction> fines(Long studentId, LocalDate date, Pageable pageable);
}
