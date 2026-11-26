package com.example.library.dto;

import com.example.library.entity.Book;
import com.example.library.entity.Student;
import com.example.library.entity.StudentStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class CatalogDto {
    private CatalogDto() {
    }

    public record BookInput(
            @NotBlank @Size(max = 200) String title,
            @NotBlank @Size(max = 160) String author,
            @NotBlank @Size(max = 20) String isbn,
            @NotBlank @Size(max = 80) String category,
            @NotBlank @Size(max = 160) String publisher,
            @NotNull @Min(1) Integer publicationYear,
            @NotNull @Min(0) Integer totalCopies) {
        public Book toEntity() {
            return new Book(title.strip(), author.strip(), isbn.strip(), category.strip(), publisher.strip(),
                    publicationYear, totalCopies, totalCopies);
        }
    }

    public record BookView(Long id, String title, String author, String isbn, String category,
                           String publisher, int publicationYear, int totalCopies, int availableCopies) {
        public static BookView from(Book book) {
            return new BookView(book.getId(), book.getTitle(), book.getAuthor(), book.getIsbn(), book.getCategory(),
                    book.getPublisher(), book.getPublicationYear(), book.getTotalCopies(), book.getAvailableCopies());
        }
    }

    public record StudentInput(
            @NotBlank @Size(max = 40) String rollNumber,
            @NotBlank @Size(max = 160) String name,
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(max = 32) String phone,
            @NotBlank @Size(max = 120) String department,
            @NotNull @Min(1) Integer academicYear,
            @NotNull StudentStatus status) {
        public Student toEntity() {
            return new Student(rollNumber.strip(), name.strip(), email.strip(), phone.strip(), department.strip(),
                    academicYear, status);
        }
    }

    public record StudentView(Long id, String rollNumber, String name, String email, String phone,
                              String department, int academicYear, StudentStatus status) {
        public static StudentView from(Student student) {
            return new StudentView(student.getId(), student.getRollNumber(), student.getName(), student.getEmail(),
                    student.getPhone(), student.getDepartment(), student.getAcademicYear(), student.getStatus());
        }
    }
}
