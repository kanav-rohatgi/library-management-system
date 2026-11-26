package com.example.library.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "reservations", indexes = {
        @Index(name = "ix_reservations_student_status", columnList = "student_id, status"),
        @Index(name = "ix_reservations_book_status_reserved_at", columnList = "book_id, status, reserved_at")
})
public class Reservation {
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
    private Instant reservedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ReservationStatus status;

    protected Reservation() {
    }

    public Reservation(Book book, Student student, Instant reservedAt, ReservationStatus status) {
        this.book = book;
        this.student = student;
        this.reservedAt = reservedAt;
        this.status = status;
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

    public Instant getReservedAt() {
        return reservedAt;
    }

    public ReservationStatus getStatus() {
        return status;
    }
}
