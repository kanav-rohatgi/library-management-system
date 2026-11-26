package com.example.library.controller;

import com.example.library.dto.CirculationDto.ReservationView;
import com.example.library.dto.CirculationDto.ReserveInput;
import com.example.library.dto.PageView;
import com.example.library.security.LibraryPrincipal;
import com.example.library.service.CirculationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/reservations")
@PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'STUDENT')")
public class ReservationController {
    private static final Set<String> SORT_FIELDS = Set.of("id", "reservedAt", "status");
    private final CirculationService circulation;

    public ReservationController(CirculationService circulation) {
        this.circulation = circulation;
    }

    @GetMapping
    public PageView<ReservationView> list(@AuthenticationPrincipal LibraryPrincipal principal,
                                          @PageableDefault(size = 20, sort = "reservedAt",
                                                  direction = Sort.Direction.DESC) Pageable pageable) {
        return circulation.reservations(principal.studentId(), ApiPaging.withAllowedSort(pageable, SORT_FIELDS));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('STUDENT')")
    public ReservationView reserve(@AuthenticationPrincipal LibraryPrincipal principal, @Valid @RequestBody ReserveInput input) {
        return circulation.reserve(principal.studentId(), input.bookId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@AuthenticationPrincipal LibraryPrincipal principal, @PathVariable Long id) {
        circulation.cancelReservation(id, principal.studentId());
    }
}
