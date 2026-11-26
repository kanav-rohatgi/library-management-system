package com.example.library.service;

import com.example.library.dto.CirculationDto.DashboardView;
import com.example.library.dto.CirculationDto.IssueInput;
import com.example.library.dto.CirculationDto.LoanView;
import com.example.library.dto.CirculationDto.ReservationView;
import com.example.library.dto.PageView;
import org.springframework.data.domain.Pageable;

public interface CirculationService {
    LoanView issue(IssueInput input);

    LoanView returnLoan(Long id);

    PageView<LoanView> history(Long studentId, Long bookId, boolean activeOnly, Pageable pageable);

    PageView<LoanView> overdue(Pageable pageable);

    PageView<LoanView> fines(Long studentId, Pageable pageable);

    ReservationView reserve(Long studentId, Long bookId);

    PageView<ReservationView> reservations(Long studentId, Pageable pageable);

    void cancelReservation(Long id, Long studentId);

    DashboardView dashboard();
}
