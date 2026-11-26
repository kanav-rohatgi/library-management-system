package com.example.library.controller;

import com.example.library.dto.CirculationDto.DashboardView;
import com.example.library.service.CirculationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
public class DashboardController {
    private final CirculationService circulation;

    public DashboardController(CirculationService circulation) {
        this.circulation = circulation;
    }

    @GetMapping
    public DashboardView dashboard() {
        return circulation.dashboard();
    }
}
