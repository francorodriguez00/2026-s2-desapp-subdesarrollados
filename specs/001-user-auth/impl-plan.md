# Implementation Plan: User Authentication

## Technical Context
*   **Database**: PostgreSQL
*   **Authentication**: Email/Password
*   **Authorization**: JWT-based, time-limited, issued on successful login.
*   **Security**: Non-specific error messages (don't reveal if email/password is wrong).
*   **Design Constraints**:
    *   Single Authentication Controller.
    *   JWT Generation/Validation: Abstracted behind a strategy (interface) to allow mockable/deterministic testing.
    *   Password Hashing: Existing hashing strategy to be reused.
    *   Authorization Filter: Transversal filter/interceptor, not controller-level.
    *   JWT Expiration: Configurable value.

## Constitution Check
- Compliance check: The plan uses the existing hashing strategy, separates authentication logic into its own controller, and uses a filter for authorization. All patterns align with the required architectural guidelines.

## Phase 0: Research
- **Research Topic**: Best practices for implementing JWT authentication filters in the specific framework used (needs exploration of existing project structure).
- **Decision**: Use a centralized filter/interceptor for JWT validation.
- **Rationale**: Keeps controllers clean and ensures uniform protection across all endpoints.

## Phase 1: Design & Contracts
### Data Model
*   **User** (Existing): email, password_hash.
*   **Token (Implied)**: Not persisted, handled via JWT claims (subject, expiration).

### Contracts
*   **POST /auth/login**
    *   Request: `{ "email": "...", "password": "..." }`
    *   Response 200: `{ "token": "..." }`
    *   Response 401: `{ "message": "Invalid credentials" }` (Standardized error)

### Quickstart Validation Guide
1.  **Prerequisites**: DB running, app configured for local profile.
2.  **Test Login**: Perform `POST /auth/login` with valid and invalid credentials. Ensure both return identical responses (401).
3.  **Validate Token**: Use valid token from successful login to call a protected endpoint.
4.  **Expire/Invalid Token**: Call protected endpoint with expired or tampered token. Expect 403 or 401.
