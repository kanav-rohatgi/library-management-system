package com.example.library.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.hibernate.annotations.Check;

@Entity
@Table(name = "books", indexes = {
        @Index(name = "ix_books_title", columnList = "title"),
        @Index(name = "ix_books_author", columnList = "author"),
        @Index(name = "ix_books_category", columnList = "category")
})
@Check(constraints = "total_copies >= 0 AND available_copies BETWEEN 0 AND total_copies")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 160)
    private String author;

    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(nullable = false, length = 80)
    private String category;

    @Column(nullable = false, length = 160)
    private String publisher;

    @Column(nullable = false)
    private int publicationYear;

    @Column(nullable = false)
    private int totalCopies;

    @Column(nullable = false)
    private int availableCopies;

    protected Book() {
    }

    public Book(String title, String author, String isbn, String category,
                String publisher, int publicationYear, int totalCopies, int availableCopies) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.category = category;
        this.publisher = publisher;
        this.publicationYear = publicationYear;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getCategory() {
        return category;
    }

    public String getPublisher() {
        return publisher;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }
}
