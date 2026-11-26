package com.example.library.controller;

import com.example.library.dto.CirculationDto.IssueInput;
import com.example.library.dto.CirculationDto.LoanView;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/transactions")
@PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
public class TransactionController {
    private static final Set<String> SORT_FIELDS = Set.of("id", "issueDate", "dueDate", "returnDate", "fineAmount");
    private final CirculationService circulation;

    public TransactionController(CirculationService circulation) {
        this.circulation = circulation;
    }

    @PostMapping("/issue")
    @ResponseStatus(HttpStatus.CREATED)
    public LoanView issue(@Valid @RequestBody IssueInput input) {
        return circulation.issue(input);
    }

    @PostMapping("/{id}/return")
    public LoanView returnLoan(@PathVariable Long id) {
        return circulation.returnLoan(id);
    }

    @GetMapping
    public PageView<LoanView> history(@RequestParam(required = false) Long studentId,
                                      @RequestParam(required = false) Long bookId,
                                      @RequestParam(defaultValue = "false") boolean activeOnly,
                                      @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC)
                                      Pageable pageable) {
        return circulation.history(studentId, bookId, activeOnly, ApiPaging.withAllowedSort(pageable, SORT_FIELDS));
    }

    @GetMapping("/overdue")
    public PageView<LoanView> overdue(@PageableDefault(size = 20, sort = "dueDate") Pageable pageable) {
        return circulation.overdue(ApiPaging.withAllowedSort(pageable, SORT_FIELDS));
    }

    @GetMapping("/fines")
    public PageView<LoanView> fines(@RequestParam(required = false) Long studentId,
                                    @PageableDefault(size = 20, sort = "dueDate") Pageable pageable) {
        return circulation.fines(studentId, ApiPaging.withAllowedSort(pageable, SORT_FIELDS));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public PageView<LoanView> ownHistory(@AuthenticationPrincipal LibraryPrincipal principal,
                                         @RequestParam(defaultValue = "false") boolean activeOnly,
                                         @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC)
                                         Pageable pageable) {
        return circulation.history(principal.studentId(), null, activeOnly,
                ApiPaging.withAllowedSort(pageable, SORT_FIELDS));
    }

    @GetMapping("/me/fines")
    @PreAuthorize("hasRole('STUDENT')")
    public PageView<LoanView> ownFines(@AuthenticationPrincipal LibraryPrincipal principal,
                                       @PageableDefault(size = 20, sort = "dueDate") Pageable pageable) {
        return circulation.fines(principal.studentId(), ApiPaging.withAllowedSort(pageable, SORT_FIELDS));
    }
}
