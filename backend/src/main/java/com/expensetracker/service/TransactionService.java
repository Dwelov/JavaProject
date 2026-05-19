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

    public List<Transaction> getAllForUser(com.expensetracker.model.User user) {
        return repo.findByUser(user);
    }

    public Transaction addForUser(Transaction t, com.expensetracker.model.User user) {
        t.setUser(user);
        return repo.save(t);
    }

    public void deleteForUser(Long id, com.expensetracker.model.User user) {
        Transaction tx = repo.findById(id).filter(tr -> tr.getUser().equals(user)).orElseThrow();
        repo.delete(tx);
    }

    public double totalIncome(com.expensetracker.model.User user) {
        Double val = repo.sumIncome(user);
        return val == null ? 0.0 : val;
    }

    public double totalExpenses(com.expensetracker.model.User user) {
        Double val = repo.sumExpenses(user);
        return val == null ? 0.0 : val;
    }

    public double balance(com.expensetracker.model.User user) {
        return totalIncome(user) - totalExpenses(user);
    }

    public Map<String, Double> expensesByCategory(com.expensetracker.model.User user) {
        return repo.findByIncomeFalseAndUser(user).stream()
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(Transaction::getAmount)
                ));
    }

    public double[] weeklyExpenses(com.expensetracker.model.User user) {
        double[] weeks = new double[4];
        for (Transaction t : repo.findByIncomeFalseAndUser(user)) {
            try {
                int day  = Integer.parseInt(t.getDate().split(" ")[0]);
                int week = Math.min((day - 1) / 7, 3);
                weeks[week] += t.getAmount();
            } catch (Exception ignored) {}
        }
        return weeks;
    }

    public Map<String, Object> summaryForUser(com.expensetracker.model.User user) {
        double income   = totalIncome(user);
        double expenses = totalExpenses(user);
        double balance  = income - expenses;
        double rate     = income > 0 ? (balance / income) * 100 : 0;
        double[] weeks  = weeklyExpenses(user);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("totalIncome",    income);
        map.put("totalExpenses",  expenses);
        map.put("balance",        balance);
        map.put("savingsRate",    rate);
        map.put("weeklyExpenses", weeks);
        map.put("byCategory",     expensesByCategory(user));
        return map;
    }
}

