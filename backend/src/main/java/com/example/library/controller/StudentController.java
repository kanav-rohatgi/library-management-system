package com.example.library.controller;

import com.example.library.dto.AccountDto.AccountView;
import com.example.library.dto.AccountDto.NewAccountInput;
import com.example.library.dto.CatalogDto.StudentInput;
import com.example.library.dto.CatalogDto.StudentView;
import com.example.library.dto.PageView;
import com.example.library.service.AccountService;
import com.example.library.service.CatalogService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Set;

@RestController
@RequestMapping("/api/students")
@PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
public class StudentController {
    private static final Set<String> SORT_FIELDS = Set.of("id", "rollNumber", "name", "department",
            "academicYear", "status");
    private final CatalogService catalog;
    private final AccountService accounts;

    public StudentController(CatalogService catalog, AccountService accounts) {
        this.catalog = catalog;
        this.accounts = accounts;
    }

    @GetMapping
    public PageView<StudentView> list(@PageableDefault(size = 20, sort = "rollNumber") Pageable pageable) {
        return catalog.students(ApiPaging.withAllowedSort(pageable, SORT_FIELDS));
    }

    @GetMapping("/{id}")
    public StudentView get(@PathVariable Long id) {
        return catalog.student(id);
    }

    @PostMapping
    public ResponseEntity<StudentView> create(@Valid @RequestBody StudentInput input) {
        StudentView student = catalog.createStudent(input);
        return ResponseEntity.created(URI.create("/api/students/" + student.id())).body(student);
    }

    @PutMapping("/{id}")
    public StudentView update(@PathVariable Long id, @Valid @RequestBody StudentInput input) {
        return catalog.updateStudent(id, input);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        catalog.deleteStudent(id);
    }

    @PostMapping("/{id}/account")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountView createAccount(@PathVariable Long id, @Valid @RequestBody NewAccountInput input) {
        return accounts.createStudentAccount(id, input);
    }
}
