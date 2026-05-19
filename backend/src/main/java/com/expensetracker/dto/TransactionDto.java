package com.expensetracker.dto;

import lombok.Data;

@Data
public class TransactionDto {
    private Long id;
    private String title;
    private String category;
    private String date;
    private Double amount;
    private boolean income;
    // Leave out user (for response safety)
}
