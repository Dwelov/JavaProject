package com.expensetracker;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Transaction {
    private Long id;
    private String title;
    private String category;
    private double amount;
    private String date;
    
    @JsonProperty("income")
    private boolean income;

    public Transaction() {}

    public Transaction(String title, String category, double amount, String date, boolean income) {
        this.title = title;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.income = income;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    @JsonProperty("income")
    public boolean isIncome() { return income; }
    
    @JsonProperty("income")
    public void setIncome(boolean income) { this.income = income; }
}