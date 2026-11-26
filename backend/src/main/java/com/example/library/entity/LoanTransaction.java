package com.example.library.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Check;

import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "loan_transactions", indexes = {
        @Index(name = "ix_loan_transactions_student_return_date", columnList = "student_id, return_date"),
        @Index(name = "ix_loan_transactions_book_return_date", columnList = "book_id, return_date"),
        @Index(name = "ix_loan_transactions_return_date_due_date", columnList = "return_date, due_date")
})
@Check(constraints = "due_date >= issue_date AND (return_date IS NULL OR return_date >= issue_date) AND fine_amount >= 0")
public class LoanTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private LocalDate issueDate;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column
    private LocalDate returnDate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal fineAmount;

    protected LoanTransaction() {
    }

    public LoanTransaction(Book book, Student student, LocalDate issueDate, LocalDate dueDate,
                           LocalDate returnDate, BigDecimal fineAmount) {
        this.book = book;
        this.student = student;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.fineAmount = fineAmount;
    }

    public Long getId() {
        return id;
    }

    public Book getBook() {
        return book;
    }

    public Student getStudent() {
        return student;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public BigDecimal getFineAmount() {
        return fineAmount;
    }

    public void complete(LocalDate date, BigDecimal fine) {
        returnDate = date;
        fineAmount = fine;
    }
}
