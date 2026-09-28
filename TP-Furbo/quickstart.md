# Quickstart Validation Guide

## Prerequisites
- Docker Desktop
- PostgreSQL running on localhost:5432
- Database `tp-furbo` exists

## Setup
1. Configure `application-local.properties` with:
   spring.datasource.url=jdbc:postgresql://localhost:5432/tp-furbo
   spring.datasource.username=postgres
   spring.datasource.password=root

## Run Scenarios
1. Register user:
   curl -X POST http://localhost:8080/users -d '{"email":"test@test.com", "name":"Test", "password":"password"}' -H "Content-Type: application/json"

2. Verify Portfolio existence:
   - Check database `portfolios` table for row with `user_id` equal to created user.

## Running Tests
- ./gradlew test
