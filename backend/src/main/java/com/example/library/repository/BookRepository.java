package com.example.library.repository;

import com.example.library.entity.Book;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
    Optional<Book> findByIsbn(String isbn);

    Optional<Book> findByIdAndArchivedFalse(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Book b where b.id = :id and b.archived = false")
    Optional<Book> lockActive(Long id);

    long countByArchivedFalse();

    @Query("select b.category as category, count(b) as total from Book b where b.archived = false group by b.category order by b.category")
    List<CategoryCount> countByCategory();

    interface CategoryCount {
        String getCategory();

        long getTotal();
    }
}
