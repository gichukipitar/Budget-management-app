package com.sirhpitar.budget.repository;

import com.sirhpitar.budget.entities.LoginChallenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginChallengeRepository extends JpaRepository<LoginChallenge, Long> {
    Optional<LoginChallenge> findByTokenHash(String tokenHash);
}