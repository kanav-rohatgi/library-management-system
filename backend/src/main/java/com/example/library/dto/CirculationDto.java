package com.example.library.dto;

import com.example.library.entity.LoanTransaction;
import com.example.library.entity.Reservation;
import com.example.library.entity.ReservationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

public final class CirculationDto {
    private CirculationDto() {
    }

    public record IssueInput(@NotNull @Positive Long bookId, @NotNull @Positive Long studentId) {
    }

    public record ReserveInput(@NotNull @Positive Long bookId) {
    }

    public record LoanView(Long id, Long bookId, String bookTitle, Long studentId, String studentName,
                           LocalDate issueDate, LocalDate dueDate, LocalDate returnDate,
                           BigDecimal fineAmount, BigDecimal accruedFine, boolean overdue) {
        public static LoanView from(LoanTransaction loan, BigDecimal accruedFine, boolean overdue) {
            return new LoanView(loan.getId(), loan.getBook().getId(), loan.getBook().getTitle(),
                    loan.getStudent().getId(), loan.getStudent().getName(), loan.getIssueDate(),
                    loan.getDueDate(), loan.getReturnDate(), loan.getFineAmount(), accruedFine, overdue);
        }
    }

    public record ReservationView(Long id, Long bookId, String bookTitle, Long studentId,
                                  String studentName, Instant reservedAt, ReservationStatus status) {
        public static ReservationView from(Reservation reservation) {
            return new ReservationView(reservation.getId(), reservation.getBook().getId(),
                    reservation.getBook().getTitle(), reservation.getStudent().getId(),
                    reservation.getStudent().getName(), reservation.getReservedAt(), reservation.getStatus());
        }
    }

    public record DashboardView(long totalBooks, long totalStudents, long issued, long overdue,
                                long returned, BigDecimal totalFines, Map<String, Long> booksByCategory) {
    }
}
