package com.example.library.controller;

import com.example.library.dto.AccountDto.AccountView;
import com.example.library.dto.AccountDto.LoginInput;
import com.example.library.dto.AccountDto.LoginView;
import com.example.library.dto.AccountDto.NewAccountInput;
import com.example.library.dto.AccountDto.PasswordInput;
import com.example.library.service.AccountService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
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

import java.util.List;

@RestController
@RequestMapping("/api")
public class AccountController {
    private final AccountService accounts;

    public AccountController(AccountService accounts) {
        this.accounts = accounts;
    }

    @PostMapping("/auth/login")
    @SecurityRequirements
    public ResponseEntity<LoginView> login(@Valid @RequestBody LoginInput input) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(accounts.login(input));
    }

    @GetMapping("/librarians")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AccountView> listLibrarians() {
        return accounts.librarians();
    }

    @PostMapping("/librarians")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public AccountView createLibrarian(@Valid @RequestBody NewAccountInput input) {
        return accounts.createLibrarian(input);
    }

    @PutMapping("/librarians/{id}/password")
    @PreAuthorize("hasRole('ADMIN')")
    public AccountView updateLibrarianPassword(@PathVariable Long id, @Valid @RequestBody PasswordInput input) {
        return accounts.updateLibrarianPassword(id, input);
    }

    @DeleteMapping("/librarians/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteLibrarian(@PathVariable Long id) {
        accounts.deleteLibrarian(id);
    }
}
