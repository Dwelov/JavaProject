package com.expensetracker.service;

import com.expensetracker.model.Transaction;
import com.expensetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository repo;

    public List<Transaction> getAll() {
        return repo.findAll();
    }

    public Transaction add(Transaction t) {
        return repo.save(t);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    public double totalIncome() {
        Double val = repo.sumIncome();
        return val == null ? 0.0 : val;
    }

    public double totalExpenses() {
        Double val = repo.sumExpenses();
        return val == null ? 0.0 : val;
    }

    public double balance() {
        return totalIncome() - totalExpenses();
    }

    public Map<String, Double> expensesByCategory() {
        return repo.findByIncomeFalse().stream()
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(Transaction::getAmount)
                ));
    }

    public double[] weeklyExpenses() {
        double[] weeks = new double[4];
        for (Transaction t : repo.findByIncomeFalse()) {
            try {
                int day  = Integer.parseInt(t.getDate().split(" ")[0]);
                int week = Math.min((day - 1) / 7, 3);
                weeks[week] += t.getAmount();
            } catch (Exception ignored) {}
        }
        return weeks;
    }

    public Map<String, Object> summary() {
        double income   = totalIncome();
        double expenses = totalExpenses();
        double balance  = income - expenses;
        double rate     = income > 0 ? (balance / income) * 100 : 0;
        double[] weeks  = weeklyExpenses();

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("totalIncome",    income);
        map.put("totalExpenses",  expenses);
        map.put("balance",        balance);
        map.put("savingsRate",    rate);
        map.put("weeklyExpenses", weeks);
        map.put("byCategory",     expensesByCategory());
        return map;
    }
}
