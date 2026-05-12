package com.expensetracker;

public class Transaction {
    private String title;
    private String category;
    private double amount;
    private String date;
    private boolean isIncome;

    public Transaction(String title, String category, double amount, String date, boolean isIncome) {
        this.title = title;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.isIncome = isIncome;
    }

    // Getters
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public double getAmount() { return amount; }
    public String getDate() { return date; }
    public boolean isIncome() { return isIncome; }
}