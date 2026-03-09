package com.sirhpitar.budget.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private int failedLoginAttempts = 0;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private boolean emailVerified = false;

    private String emailVerificationToken;
    private Instant emailVerificationTokenExpiry;
    private Instant emailVerificationSentAt;

    @Column(nullable = false)
    private boolean termsAccepted = false;

    private String currency;
    private String timezone;
    private String profilePictureUrl;

    private String passwordResetTokenHash;
    private Instant passwordResetTokenExpiry;
    private Instant passwordResetRequestedAt;

    @Column(nullable = false)
    private boolean twoFactorEnabled = false;

    private String twoFactorSecret;
    private Instant lockedUntil;
}
