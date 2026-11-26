package com.example.library.service;

import com.example.library.dto.CatalogDto.BookInput;
import com.example.library.dto.CatalogDto.BookView;
import com.example.library.dto.CatalogDto.StudentInput;
import com.example.library.dto.CatalogDto.StudentView;
import com.example.library.dto.PageView;
import org.springframework.data.domain.Pageable;

public interface CatalogService {
    PageView<BookView> books(String query, String category, Boolean available, Pageable pageable);

    BookView book(Long id);

    BookView createBook(BookInput input);

    BookView updateBook(Long id, BookInput input);

    void deleteBook(Long id);

    PageView<StudentView> students(Pageable pageable);

    StudentView student(Long id);

    StudentView createStudent(StudentInput input);

    StudentView updateStudent(Long id, StudentInput input);

    void deleteStudent(Long id);
}
