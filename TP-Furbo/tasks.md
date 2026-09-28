# User Registration Tasks

## Dependencies
- Foundational -> US1

## Phase 1: Setup
- [X] T001 Define dependencies in build.gradle for Spring Web, Data JPA, Security, PostgreSQL, Testcontainers

## Phase 2: Foundational
- [X] T002 Define User entity in src/main/java/com/furbo/model/User.java
- [X] T003 Define Portfolio entity in src/main/java/com/furbo/model/Portfolio.java
- [X] T004 Define UserRepository in src/main/java/com/furbo/repository/UserRepository.java
- [X] T005 Define PortfolioRepository in src/main/java/com/furbo/repository/PortfolioRepository.java
- [X] T006 Define PasswordHasher interface in src/main/java/com/furbo/security/PasswordHasher.java
- [X] T007 Define BCryptPasswordHasher implementation in src/main/java/com/furbo/security/BCryptPasswordHasher.java

## Phase 3: [US1] User Registration
- [X] T008 [US1] Create UserDTO in src/main/java/com/furbo/adapter/dto/UserRegistrationDTO.java
- [X] T009 [US1] Create UserRegistrationController in src/main/java/com/furbo/controller/UserController.java
- [X] T010 [US1] Implement UserService with registration and portfolio creation in src/main/java/com/furbo/service/UserService.java
- [X] T011 [US1] Create integration test using Testcontainers in src/test/java/com/furbo/integration/UserRegistrationIntegrationTest.java

## Phase 4: Polish
- [X] T012 Configure exception handling for unique email constraint in src/main/java/com/furbo/controller/GlobalExceptionHandler.java
