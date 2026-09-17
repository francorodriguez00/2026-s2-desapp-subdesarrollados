# User Registration Tasks

## Dependencies
- Foundational -> US1

## Phase 1: Setup
- [ ] T001 Define dependencies in build.gradle for Spring Web, Data JPA, Security, PostgreSQL, Testcontainers

## Phase 2: Foundational
- [ ] T002 Define User entity in src/main/java/com/furbo/model/User.java
- [ ] T003 Define Portfolio entity in src/main/java/com/furbo/model/Portfolio.java
- [ ] T004 Define UserRepository in src/main/java/com/furbo/repository/UserRepository.java
- [ ] T005 Define PortfolioRepository in src/main/java/com/furbo/repository/PortfolioRepository.java
- [ ] T006 Define PasswordHasher interface in src/main/java/com/furbo/security/PasswordHasher.java
- [ ] T007 Define BCryptPasswordHasher implementation in src/main/java/com/furbo/security/BCryptPasswordHasher.java

## Phase 3: [US1] User Registration
- [ ] T008 [US1] Create UserDTO in src/main/java/com/furbo/adapter/dto/UserRegistrationDTO.java
- [ ] T009 [US1] Create UserRegistrationController in src/main/java/com/furbo/controller/UserController.java
- [ ] T010 [US1] Implement UserService with registration and portfolio creation in src/main/java/com/furbo/service/UserService.java
- [ ] T011 [US1] Create integration test using Testcontainers in src/test/java/com/furbo/integration/UserRegistrationIntegrationTest.java

## Phase 4: Polish
- [ ] T012 Configure exception handling for unique email constraint in src/main/java/com/furbo/controller/GlobalExceptionHandler.java
