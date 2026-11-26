package com.example.library.dto;

import com.example.library.entity.LibraryUser;
import com.example.library.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AccountDto {
    private AccountDto() {
    }

    public record LoginInput(@NotBlank @Size(max = 80) String username,
                             @NotBlank @Size(max = 72) String password) {
    }

    public record LoginView(String token, String username, Role role, Long studentId) {
    }

    public record NewAccountInput(
            @NotBlank @Size(max = 80) @Pattern(regexp = "[A-Za-z0-9._-]+") String username,
            @NotBlank @Size(min = 8, max = 72) String password) {
    }

    public record PasswordInput(@NotBlank @Size(min = 8, max = 72) String password) {
    }

    public record AccountView(Long id, String username, Role role) {
        public static AccountView from(LibraryUser user) {
            return new AccountView(user.getId(), user.getUsername(), user.getRole());
        }
    }
}
