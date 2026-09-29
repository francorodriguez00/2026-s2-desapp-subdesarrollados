# Quickstart Validation Guide

### Prerequisites
*   PostgreSQL running on `jdbc:postgresql://localhost:5432/furbo_db`.
*   Application configured with local profile (`user: postgres / password: root`).

### Validation Scenarios

1.  **Authentication Endpoint**
    *   **Action**: `POST /auth/login` with `{ "email": "test@example.com", "password": "password" }`.
    *   **Success**: Returns HTTP 200 and a JWT.
    *   **Failure (Email)**: `POST /auth/login` with non-existent email returns HTTP 401.
    *   **Failure (Password)**: `POST /auth/login` with correct email but wrong password returns HTTP 401.

2.  **Protected Endpoint Access**
    *   **Action**: Access any market operation endpoint (e.g., `GET /portfolio`) without a token.
    *   **Result**: HTTP 401 Unauthorized.

3.  **Protected Endpoint Access (Authorized)**
    *   **Action**: Access `GET /portfolio` with the `Authorization: Bearer <token>` header.
    *   **Result**: Successful access.

4.  **Token Expiration**
    *   **Action**: Use an expired token.
    *   **Result**: HTTP 401/403.
