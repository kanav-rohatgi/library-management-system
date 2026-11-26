package com.example.library.repository;

import com.example.library.entity.LibraryUser;
import com.example.library.entity.Role;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LibraryUserRepository extends JpaRepository<LibraryUser, Long> {
    @EntityGraph(attributePaths = "student")
    Optional<LibraryUser> findByUsername(String username);

    @EntityGraph(attributePaths = "student")
    Optional<LibraryUser> findById(Long id);

    Optional<LibraryUser> findByIdAndRole(Long id, Role role);

    Optional<LibraryUser> findByStudentId(Long studentId);

    List<LibraryUser> findAllByRoleOrderByUsername(Role role);
}
