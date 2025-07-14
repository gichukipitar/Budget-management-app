package com.sirhpitar.budget.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Setter
@Getter
@Table(name = "income_sources")
public class IncomeSource extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String name; // "Salary", "Freelance", etc.
    private String type; // "Salary", "Bonus", "Freelance", etc.
    private String category; // Optional grouping
    private Double amount;
    private String frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;
    private String description;
}