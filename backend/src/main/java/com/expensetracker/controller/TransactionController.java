package com.expensetracker.controller;

import com.expensetracker.dto.TransactionDtoMapper;

import com.expensetracker.model.Transaction;
import com.expensetracker.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService service;
    private final com.expensetracker.repository.UserRepository userRepo;

    private com.expensetracker.model.User getCurrentUser() {
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        return userRepo.findByEmail(email).orElseThrow();
    }

    @GetMapping
    public List<com.expensetracker.dto.TransactionDto> getAll() {
        return service.getAllForUser(getCurrentUser())
                .stream()
                .map(TransactionDtoMapper::toDto)
                .toList();
    }

    @PostMapping
    public ResponseEntity<com.expensetracker.dto.TransactionDto> add(@Valid @RequestBody Transaction transaction) {
        return ResponseEntity.ok(TransactionDtoMapper.toDto(service.addForUser(transaction, getCurrentUser())));
    }

@DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteForUser(id, getCurrentUser());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> summary() {
        return ResponseEntity.ok(service.summaryForUser(getCurrentUser()));
    }
}

