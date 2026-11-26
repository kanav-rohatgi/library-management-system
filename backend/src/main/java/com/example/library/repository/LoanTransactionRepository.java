package com.example.library.repository;

import com.example.library.entity.LoanTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanTransactionRepository extends JpaRepository<LoanTransaction, Long> {
}
