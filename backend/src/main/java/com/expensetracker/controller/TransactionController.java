package com.expensetracker.controller;

import com.expensetracker.model.User;
import com.expensetracker.model.Transaction;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.service.TransactionService;
import com.expensetracker.dto.TransactionDtoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService service;
    private final UserRepository userRepo;

    private User getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }
        String email = auth.getPrincipal().toString();
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
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

