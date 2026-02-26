package com.sirhpitar.budget.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    /**
     * Store BCrypt hash here (never store raw password)
     */
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private int failedLoginAttempts = 0;

    // Profile fields
    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    // Verification fields
    @Column(nullable = false)
    private boolean emailVerified = false;

    private String emailVerificationToken;
    private Instant emailVerificationTokenExpiry;
    private Instant emailVerificationSentAt;

    // Terms acceptance
    @Column(nullable = false)
    private boolean termsAccepted = false;

    // Preferences
    private String currency;
    private String timezone;

    // Optional: profile picture
    private String profilePictureUrl;


    private Instant lockedUntil;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Budget> budgets;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Expense> expenses;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExpenseCategory> expenseCategories;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IncomeSource> incomeSources;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FinancialGoal> financialGoals;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notifications;
}
