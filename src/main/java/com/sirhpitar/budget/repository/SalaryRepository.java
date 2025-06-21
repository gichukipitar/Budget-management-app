package com.sirhpitar.budget.repository;

import com.sirhpitar.budget.entities.Salary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalaryRepository extends JpaRepository<Salary, Long> {
}
