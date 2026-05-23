package com.expensetracker;

import com.expensetracker.model.Transaction;
import com.expensetracker.model.User;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final TransactionRepository transactionRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        User admin = userRepo.findByEmail("admin@financeos.com").orElseGet(() ->
            userRepo.save(User.builder()
                .fullName("Admin User")
                .email("admin@financeos.com")
                .password(passwordEncoder.encode("password123"))
                .build())
        );

        if (transactionRepo.count() == 0) {
            transactionRepo.saveAll(List.of(
                build("Freelance UI Project", "Income",        65000, "01 May 2026", true,  admin),
                build("Salary",               "Income",        90000, "05 May 2026", true,  admin),
                build("Selling Old Laptop",   "Income",        25000, "10 May 2026", true,  admin),
                build("Grocery Shopping",     "Food & Dining",  4500, "02 May 2026", false, admin),
                build("KFC Dinner",           "Food & Dining",  2200, "07 May 2026", false, admin),
                build("Bakery",               "Food & Dining",   800, "12 May 2026", false, admin),
                build("Dine-out with Family", "Food & Dining",  5500, "18 May 2026", false, admin),
                build("Electricity Bill",     "Utilities",      3500, "03 May 2026", false, admin),
                build("Internet Bill",        "Utilities",      3000, "04 May 2026", false, admin),
                build("Gas Bill",             "Utilities",      1200, "04 May 2026", false, admin),
                build("Gym Membership",       "Health",         2500, "01 May 2026", false, admin),
                build("Doctor Visit",         "Health",         1500, "09 May 2026", false, admin),
                build("Medicines",            "Health",          900, "14 May 2026", false, admin),
                build("Fuel",                 "Transport",      4000, "06 May 2026", false, admin),
                build("Rickshaw Fares",       "Transport",       600, "11 May 2026", false, admin),
                build("Clothes - Eid",        "Shopping",       8000, "15 May 2026", false, admin),
                build("Shoes",                "Shopping",       4500, "20 May 2026", false, admin),
                build("Online Course",        "Education",      3500, "08 May 2026", false, admin),
                build("Movie Tickets",        "Entertainment",  1400, "16 May 2026", false, admin),
                build("Netflix Subscription", "Entertainment",   900, "05 May 2026", false, admin)
            ));
        }
    }

    private Transaction build(String title, String category, double amount, String date, boolean income, User user) {
        return Transaction.builder()
            .title(title)
            .category(category)
            .amount(amount)
            .date(date)
            .income(income)
            .user(user)
           .build();
    }
}