package com.example.library.repository;

import com.example.library.entity.Reservation;
import com.example.library.entity.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByStudentIdAndBookIdAndStatus(Long studentId, Long bookId, ReservationStatus status);

    @Modifying
    @Query("""
            update Reservation r set r.status = :next
            where r.id = :id and r.status = 'PENDING'
            """)
    int transitionPending(Long id, ReservationStatus next);

    @Modifying
    @Query("""
            update Reservation r set r.status = 'CANCELLED'
            where r.status = 'PENDING'
              and (:bookId is null or r.book.id = :bookId)
              and (:studentId is null or r.student.id = :studentId)
            """)
    void cancelPending(Long bookId, Long studentId);

    @EntityGraph(attributePaths = {"book", "student"})
    @Query("select r from Reservation r where :studentId is null or r.student.id = :studentId")
    Page<Reservation> list(Long studentId, Pageable pageable);
}
