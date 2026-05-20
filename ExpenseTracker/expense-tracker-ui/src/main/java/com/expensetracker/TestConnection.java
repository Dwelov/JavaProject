package com.expensetracker;

public class TestConnection {
    public static void main(String[] args) {
        try {
            Object response = ApiClient.get("/ping", Object.class);
            System.out.println("Response: " + response);
            System.out.println("Connection successful!");
        } catch (Exception e) {
            System.err.println("Connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
