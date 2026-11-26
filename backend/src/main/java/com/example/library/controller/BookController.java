package com.example.library.controller;

import com.example.library.dto.CatalogDto.BookInput;
import com.example.library.dto.CatalogDto.BookView;
import com.example.library.dto.PageView;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Set;

@RestController
@RequestMapping("/api/books")
public class BookController {
    private static final Set<String> SORT_FIELDS = Set.of("id", "title", "author", "isbn", "category",
            "publisher", "publicationYear", "totalCopies", "availableCopies");
    private final CatalogService catalog;

    public BookController(CatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'STUDENT')")
    public PageView<BookView> list(@RequestParam(required = false) String q,
                                   @RequestParam(required = false) String category,
                                   @RequestParam(required = false) Boolean available,
                                   @PageableDefault(size = 20, sort = "title") Pageable pageable) {
        return catalog.books(q, category, available, ApiPaging.withAllowedSort(pageable, SORT_FIELDS));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'STUDENT')")
    public BookView get(@PathVariable Long id) {
        return catalog.book(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<BookView> create(@Valid @RequestBody BookInput input) {
        BookView book = catalog.createBook(input);
        return ResponseEntity.created(URI.create("/api/books/" + book.id())).body(book);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public BookView update(@PathVariable Long id, @Valid @RequestBody BookInput input) {
        return catalog.updateBook(id, input);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public void delete(@PathVariable Long id) {
        catalog.deleteBook(id);
    }
}
