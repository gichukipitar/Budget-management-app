package com.sirhpitar.budget.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Setter
@Getter
@Table(
        name = "expenses",
        uniqueConstraints = @UniqueConstraint(
                name = "unique_user_category_amount_date",
                columnNames = {"user_id", "expense_category_id", "amount", "transaction_date"}
        )
)
public class Expense extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "expense_category_id", nullable = false)
    private ExpenseCategory expenseCategory;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Double amount;
    private String description;
    private LocalDate transactionDate;
    private String paymentMethod;
    private String receiptUrl;
    private boolean recurring;
    private String recurringFrequency;
}