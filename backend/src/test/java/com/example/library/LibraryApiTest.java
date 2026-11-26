package com.example.library;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "library.seed.enabled=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LibraryApiTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;

    @Test
    void rejectsMissingAndInvalidBearerTokensWithAJson401() throws Exception {
        mvc.perform(get("/api/books"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(jsonPath("$.path").value("/api/books"));
        mvc.perform(get("/api/books").header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void rejectsBadLoginWithoutIssuingAToken() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("username", "student", "password", "wrong-password"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void studentCanSearchAndReadOwnLoansButCannotUseStaffEndpoints() throws Exception {
        String token = bearer("student", "DemoStudent123!", "STUDENT");
        mvc.perform(get("/api/books").param("q", "Java").param("sort", "title,desc")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Effective Java"));
        mvc.perform(get("/api/transactions/me").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
        mvc.perform(get("/api/transactions").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied"));
        mvc.perform(delete("/api/books/1").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void onlyAdminCanListLibrarians() throws Exception {
        mvc.perform(get("/api/librarians").header(HttpHeaders.AUTHORIZATION,
                        bearer("librarian", "DemoLibrarian123!", "LIBRARIAN")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/librarians").header(HttpHeaders.AUTHORIZATION,
                        bearer("admin", "DemoAdmin123!", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("librarian"))
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist());
    }

    @Test
    void validatesRequestBodiesAndSortFields() throws Exception {
        String token = bearer("librarian", "DemoLibrarian123!", "LIBRARIAN");
        mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON).content("{}")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.totalCopies").exists());
        mvc.perform(get("/api/books").param("sort", "unknown").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unsupported sort field: unknown"));
    }

    private String bearer(String username, String password, String role) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.role").value(role))
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + json.readTree(body).get("token").asText();
    }
}
