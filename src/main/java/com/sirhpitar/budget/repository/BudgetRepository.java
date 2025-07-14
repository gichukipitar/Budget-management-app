package com.sirhpitar.budget.repository;

import com.sirhpitar.budget.entities.Budget;
import com.sirhpitar.budget.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUser(User user);
}
