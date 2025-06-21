# Technical Specification

## 1. Tech Stack

- **Language:** Java
- **Framework:** Spring Boot
- **Database:** H2, SQLite,MySQL, PostgresSQL or other Java-compatible DB (for MVP)
- **UI:** JSP/Thymeleaf (web) or REST API with frontend later or Angular/React (optional for MVP)

## 2. Architecture Overview

- MVC or layered architecture.
- Data persistence via JPA/Hibernate.

## 3. Modules

- **Salary Module:** CRUD for salary
- **Expense Module:** CRUD for expenses
- **Reporting Module:** Summaries and analytics
- **(Optional) Auth Module:** User authentication

## 4. API Design (if RESTful)

- `/salary` – manage salary entries
- `/expenses` – manage expenses
- `/reports` – retrieve summaries

## 5. Data Model

- Salary: amount, period (monthly, weekly, yearly), date
- Expense: amount, category, date, description

## 6. Dependencies

- Spring Boot Starter (if used)
- JPA/Hibernate
- Database driver

---
