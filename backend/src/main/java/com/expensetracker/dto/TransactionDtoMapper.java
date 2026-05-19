package com.expensetracker.dto;

import com.expensetracker.model.Transaction;

public class TransactionDtoMapper {
    public static TransactionDto toDto(Transaction t) {
        TransactionDto dto = new TransactionDto();
        dto.setId(t.getId());
        dto.setTitle(t.getTitle());
        dto.setCategory(t.getCategory());
        dto.setDate(t.getDate());
        dto.setAmount(t.getAmount());
        dto.setIncome(t.isIncome());
        return dto;
    }
}
