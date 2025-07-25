package com.sirhpitar.budget.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity

@Setter
@Getter
@Table(name = "notifications")
public class Notification extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String message;
    private boolean read = false;
}
