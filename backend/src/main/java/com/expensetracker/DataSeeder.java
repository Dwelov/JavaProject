package com.expensetracker;

import com.expensetracker.model.Transaction;
import com.expensetracker.model.User;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final TransactionRepository transactionRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // ── Seed default user ──
        User admin = userRepo.findByEmail("admin@financeos.com").orElseGet(() -> 
            userRepo.save(User.builder()
                .fullName("Admin User")
                .email("admin@financeos.com")
                .password(passwordEncoder.encode("password123"))
                .build())
        );

        // ── Seed initial transactions ──
        if (transactionRepo.count() == 0) {

            // Income
            save("Freelance UI Project", "Income",        65000, "01 May 2026", true, admin);
            save("Salary",               "Income",        90000, "05 May 2026", true, admin);
            save("Selling Old Laptop",   "Income",        25000, "10 May 2026", true, admin);

            // Food & Dining
            save("Grocery Shopping",     "Food & Dining",  4500, "02 May 2026", false, admin);
            save("KFC Dinner",           "Food & Dining",  2200, "07 May 2026", false, admin);
            save("Bakery",               "Food & Dining",   800, "12 May 2026", false, admin);
            save("Dine-out with Family", "Food & Dining",  5500, "18 May 2026", false, admin);

            // Utilities
            save("Electricity Bill",     "Utilities",      3500, "03 May 2026", false, admin);
            save("Internet Bill",        "Utilities",      3000, "04 May 2026", false, admin);
            save("Gas Bill",             "Utilities",      1200, "04 May 2026", false, admin);

            // Health
            save("Gym Membership",       "Health",         2500, "01 May 2026", false, admin);
            save("Doctor Visit",         "Health",         1500, "09 May 2026", false, admin);
            save("Medicines",            "Health",          900, "14 May 2026", false, admin);

            // Transport
            save("Fuel",                 "Transport",      4000, "06 May 2026", false, admin);
            save("Rickshaw Fares",       "Transport",       600, "11 May 2026", false, admin);

            // Shopping
            save("Clothes — Eid",        "Shopping",       8000, "15 May 2026", false, admin);
            save("Shoes",                "Shopping",       4500, "20 May 2026", false, admin);

            // Education
            save("Online Course",        "Education",      3500, "08 May 2026", false, admin);

            // Entertainment
            save("Movie Tickets",        "Entertainment",  1400, "16 May 2026", false, admin);
            save("Netflix Subscription", "Entertainment",   900, "05 May 2026", false, admin);
        }
    }

    private void save(String title, String category, double amount, String date, boolean income, User user) {
        transactionRepo.save(Transaction.builder()
            .title(title)
            .category(category)
            .amount(amount)
            .date(date)
            .income(income)
            .user(user)
            .build());
    }
}
