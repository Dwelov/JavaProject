package com.expensetracker;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton data store holding all transactions for the session.
 * Synchronized with the backend API.
 */
public class TransactionStore {

    private static TransactionStore instance;
    private final List<Transaction> transactions = new ArrayList<>();

    private TransactionStore() {
        // We will fetch from backend instead of seeding
    }

    public static TransactionStore getInstance() {
        if (instance == null) instance = new TransactionStore();
        return instance;
    }

    public void fetchTransactions() {
        try {
            Transaction[] fetched = ApiClient.get("/transactions", Transaction[].class);
            transactions.clear();
            if (fetched != null) {
                for (Transaction t : fetched) {
                    transactions.add(t);
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching transactions: " + e.getMessage());
        }
    }

    // ── CRUD ───────────────────────────────────────────────────────────────────

    public List<Transaction> getAll() {
        return new ArrayList<>(transactions);
    }

    public void add(Transaction t) {
        try {
            Transaction saved = ApiClient.post("/transactions", t, Transaction.class);
            if (saved != null) {
                transactions.add(saved);
            }
        } catch (Exception e) {
            System.err.println("Error adding transaction: " + e.getMessage());
        }
    }

    public void remove(Transaction t) {
        if (t.getId() == null) return;
        try {
            ApiClient.delete("/transactions/" + t.getId());
            transactions.remove(t);
        } catch (Exception e) {
            System.err.println("Error removing transaction: " + e.getMessage());
        }
    }

    // ── AGGREGATES ─────────────────────────────────────────────────────────────

    public double totalIncome() {
        return transactions.stream().filter(Transaction::isIncome)
                .mapToDouble(Transaction::getAmount).sum();
    }

    public double totalExpenses() {
        return transactions.stream().filter(t -> !t.isIncome())
                .mapToDouble(Transaction::getAmount).sum();
    }

    public double balance() {
        return totalIncome() - totalExpenses();
    }

    /** Sum of expenses per category */
    public java.util.Map<String, Double> expensesByCategory() {
        java.util.Map<String, Double> map = new java.util.LinkedHashMap<>();
        for (Transaction t : transactions) {
            if (!t.isIncome()) {
                map.merge(t.getCategory(), t.getAmount(), Double::sum);
            }
        }
        return map;
    }

    /** Weekly expense totals (weeks 1-4 based on day-of-month) */
    public double[] weeklyExpenses() {
        double[] weeks = new double[4];
        for (Transaction t : transactions) {
            if (!t.isIncome()) {
                // Parse day from "DD MMM YYYY" format
                try {
                    int day = Integer.parseInt(t.getDate().split(" ")[0]);
                    int week = Math.min((day - 1) / 7, 3);
                    weeks[week] += t.getAmount();
                } catch (Exception ignored) { }
            }
        }
        return weeks;
    }
}