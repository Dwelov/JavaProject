package com.expensetracker.repository;

import com.expensetracker.model.User;

import com.expensetracker.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUser(User user);

    List<Transaction> findByIncomeTrueAndUser(User user);

    List<Transaction> findByIncomeFalseAndUser(User user);

    List<Transaction> findByCategoryAndUser(String category, User user);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.income = true AND t.user = :user")
    Double sumIncome(User user);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.income = false AND t.user = :user")
    Double sumExpenses(User user);
}

