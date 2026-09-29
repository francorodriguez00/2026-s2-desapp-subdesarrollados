# Tasks: User Authentication

## Implementation Strategy
MVP: Complete the authentication cycle (Login + JWT issuance + Protected Endpoint access).

## Phase 1: Setup
- [ ] T001 Initialize JWT strategy interface and configuration classes.

## Phase 2: Foundational
- [ ] T002 Implement `JwtService` interface for JWT generation/validation.
- [ ] T003 Implement `JwtServiceImpl` for token generation, validation, and expiration management.
- [ ] T004 Implement custom configuration to load JWT expiration time and secret.

## Phase 3: User Story 1 - Authentication Flow
- [ ] T005 [P] [US1] Create `AuthController` for handling `/auth/login` requests.
- [ ] T006 [US1] Implement `AuthService` logic (uses existing password hashing strategy).
- [ ] T007 [US1] Ensure `AuthController` returns non-specific error messages on failure (same status/message for wrong email or wrong password).

## Phase 4: User Story 2 - Authorization Filter
- [ ] T008 [US2] Implement `JwtAuthenticationFilter` (transversal interceptor).
- [ ] T009 [US2] Configure the filter to block requests without a valid, non-expired JWT.
- [ ] T010 [US2] Protect market operation endpoints (buying/selling, viewing portfolio) using the filter.

## Phase 5: Polish & Cross-Cutting Concerns
- [ ] T011 Verify integration of `JwtService` in both AuthController and AuthenticationFilter.
- [ ] T012 Run end-to-end quickstart validation scenarios defined in `quickstart.md`.

## Dependencies
- Phase 2 (Foundational) must be completed before Phase 3 (Authentication Flow) and Phase 4 (Authorization Filter).

## Parallel Execution
- T005 and T006 can be worked on in parallel.
- T001-T004 can be partially parallelized.
