# High Level Design (HLD)
## Budget Management Application

**Document Version:** 1.0  
**Date:** 2025-07-14  
**Prepared by:** gichukipitar  
**Project:** Budget Management Application

---

## 1. Executive Summary

### 1.1 Purpose
This High Level Design document provides a comprehensive technical overview of the Budget Management Application, outlining the system architecture, component interactions, data flow, and technology stack decisions.

### 1.2 Scope
The HLD covers the complete system design including:
- Microservices architecture design
- Database design strategy
- Integration patterns
- Security architecture
- Deployment architecture on OpenShift
- Performance and scalability considerations

### 1.3 Assumptions
- Target deployment platform: Red Hat OpenShift Container Platform
- Primary backend technology: Spring Boot with Java 17+
- Database: PostgreSQL with Redis for caching
- Expected user base: 10,000+ concurrent users
- Geographic distribution: Initially single region, expandable to multi-region

---

## 2. System Architecture Overview

### 2.1 Architecture Pattern
The system follows a **Microservices Architecture** pattern with the following characteristics:
- Domain-driven service boundaries
- Independent deployability
- Technology diversity support
- Decentralized data management
- Fault tolerance and resilience

### 2.2 High-Level System Diagram
┌─────────────────────────────────────────────────────────────────────────────────┐ │ CLIENT LAYER │ ├─────────────────────────────────────────────────────────────────────────────────┤ │ ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ ┌─────────────┐ │ │ │ Web App │ │ Mobile App │ │ Admin Panel │ │ PWA │ │ │ │ (React) │ │ (React Native) │ │ (React) │ │ (React) │ │ │ └─────────────────┘ └─────────────────┘ └─────────────────┘ └─────────────┘ │ └─────────────────────────────────────────────────────────────────────────────────┘ │ ▼ ┌─────────────────────────────────────────────────────────────────────────────────┐ │ API GATEWAY LAYER │ ├─────────────────────────────────────────────────────────────────────────────────┤ │ ┌─────────────────────────────────────────────────────────────────────────────┐ │ │ │ OpenShift Router (Load Balancer) │ │ │ └─────────────────────────────────────────────────────────────────────────────┘ │ │ ┌─────────────────────────────────────────────────────────────────────────────┐ │ │ │ Spring Cloud Gateway │ │ │ │ - Authentication & Authorization - Rate Limiting │ │ │ │ - Request Routing - Circuit Breaking │ │ │ │ - CORS Handling - Request/Response Transformation │ │ │ └─────────────────────────────────────────────────────────────────────────────┘ │ └─────────────────────────────────────────────────────────────────────────────────┘ │ ▼ ┌─────────────────────────────────────────────────────────────────────────────────┐ │ MICROSERVICES LAYER │ ├─────────────────────────────────────────────────────────────────────────────────┤ │ ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │ │ │ Auth Service │ │ User Service │ │Financial Service│ │ │ │ Port: 8080 │ │ Port: 8081 │ │ Port: 8082 │ │ │ │ JWT & OAuth │ │ Profile Mgmt │ │ Income/Expense│ │ │ └─────────────────┘ └─────────────────┘ └─────────────────┘ │ │ │ │ ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │ │ │Analytics Service│ │Notification Svc │ │ File Service │ │ │ │ Port: 8083 │ │ Port: 8084 │ │ Port: 8085 │ │ │ │ Reports & AI │ │ Alerts & Push │ │ Receipt Store │ │ │ └─────────────────┘ └─────────────────┘ └─────────────────┘ │ │ │ │ ┌─────────────────┐ ┌─────────────────┐ │ │ │ Budget Service │ │ Report Service │ │ │ │ Port: 8086 │ │ Port: 8087 │ │ │ │ Budget Mgmt │ │ PDF/CSV Gen │ │ │ └─────────────────┘ └─────────────────┘ │ └─────────────────────────────────────────────────────────────────────────────────┘ │ ▼ ┌─────────────────────────────────────────────────────────────────────────────────┐ │ MESSAGE BROKER │ ├─────────────────────────────────────────────────────────────────────────────────┤ │ ┌─────────────────────────────────────────────────────────────────────────────┐ │ │ │ Apache Kafka Cluster │ │ │ │ Topics: user-events, financial-events, notification-events, audit-events │ │ │ └─────────────────────────────────────────────────────────────────────────────┘ │ └─────────────────────────────────────────────────────────────────────────────────┘ │ ▼ ┌─────────────────────────────────────────────────────────────────────────────────┐ │ DATA LAYER │ ├─────────────────────────────────────────────────────────────────────────────────┤ │ ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │ │ │ PostgreSQL │ │ Redis Cache │ │ File Storage │ │ │ │ (Primary DB) │ │ (Session/ │ │ (OpenShift │ │ │ │ - User Data │ │ Cache) │ │ Persistent │ │ │ │ - Financial │ │ - User Cache │ │ Volumes) │ │ │ │ - Audit Logs │ │ - App Cache │ │ - Receipts │ │ │ └─────────────────┘ └─────────────────┘ └─────────────────┘ │ │ │ │ ┌─────────────────┐ ┌─────────────────┐ │ │ │ Elasticsearch │ │ Prometheus │ │ │ │ (Search & │ │ (Metrics) │ │ │ │ Analytics) │ │ │ │ │ └─────────────────┘ └─────────────────┘ │ └─────────────────────────────────────────────────────────────────────────────────┘