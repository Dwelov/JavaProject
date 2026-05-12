package com.expensetracker;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton data store holding all transactions for the session.
 * Replace the seed data with DB / file I/O as needed.
 */
public class TransactionStore {

    private static TransactionStore instance;
    private final List<Transaction> transactions = new ArrayList<>();

    private TransactionStore() {
        seed();
    }

    public static TransactionStore getInstance() {
        if (instance == null) instance = new TransactionStore();
        return instance;
    }

    // ── CRUD ───────────────────────────────────────────────────────────────────

    public List<Transaction> getAll() {
        return new ArrayList<>(transactions);
    }

    public void add(Transaction t) {
        transactions.add(t);
    }

    public void remove(Transaction t) {
        transactions.remove(t);
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

    // ── SEED DATA ─────────────────────────────────────────────────────────────

    private void seed() {
        // Income
        transactions.add(new Transaction("Freelance UI Project",  "Income",         65000, "01 May 2026", true));
        transactions.add(new Transaction("Salary",                "Income",         90000, "05 May 2026", true));
        transactions.add(new Transaction("Selling Old Laptop",    "Income",         25000, "10 May 2026", true));

        // Food & Dining
        transactions.add(new Transaction("Grocery Shopping",      "Food & Dining",   4500, "02 May 2026", false));
        transactions.add(new Transaction("KFC Dinner",            "Food & Dining",   2200, "07 May 2026", false));
        transactions.add(new Transaction("Bakery",                "Food & Dining",    800, "12 May 2026", false));
        transactions.add(new Transaction("Dine-out with Family",  "Food & Dining",   5500, "18 May 2026", false));

        // Utilities
        transactions.add(new Transaction("Electricity Bill",      "Utilities",       3500, "03 May 2026", false));
        transactions.add(new Transaction("Internet Bill",         "Utilities",       3000, "04 May 2026", false));
        transactions.add(new Transaction("Gas Bill",              "Utilities",       1200, "04 May 2026", false));

        // Health
        transactions.add(new Transaction("Gym Membership",        "Health",          2500, "01 May 2026", false));
        transactions.add(new Transaction("Doctor Visit",          "Health",          1500, "09 May 2026", false));
        transactions.add(new Transaction("Medicines",             "Health",           900, "14 May 2026", false));

        // Transport
        transactions.add(new Transaction("Fuel",                  "Transport",       4000, "06 May 2026", false));
        transactions.add(new Transaction("Rickshaw Fares",        "Transport",        600, "11 May 2026", false));

        // Shopping
        transactions.add(new Transaction("Clothes — Eid",        "Shopping",        8000, "15 May 2026", false));
        transactions.add(new Transaction("Shoes",                 "Shopping",        4500, "20 May 2026", false));

        // Education
        transactions.add(new Transaction("Online Course",         "Education",       3500, "08 May 2026", false));

        // Entertainment
        transactions.add(new Transaction("Movie Tickets",         "Entertainment",   1400, "16 May 2026", false));
        transactions.add(new Transaction("Netflix Subscription",  "Entertainment",    900, "05 May 2026", false));
    }
}