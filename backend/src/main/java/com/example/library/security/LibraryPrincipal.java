package com.example.library.security;

import java.security.Principal;

public record LibraryPrincipal(String username, Long studentId) implements Principal {
    @Override
    public String getName() {
        return username;
    }
}
