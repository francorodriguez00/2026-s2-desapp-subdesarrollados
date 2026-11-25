# User Authentication

## Overview
Implement a secure authentication mechanism for the football player valuation market application. Registered users must be able to authenticate using their email and password to receive a time-limited JWT, which is required for all market operations (buying/selling player tokens, viewing portfolio).

## User Scenarios & Testing

### Scenario 1: Successful Login
*   **Given** a registered user
*   **When** the user provides valid email and password
*   **Then** the system returns a valid JWT

### Scenario 2: Failed Login (Invalid Email)
*   **Given** an unregistered email
*   **When** the user attempts to authenticate
*   **Then** the system denies access and provides a generic error message

### Scenario 3: Failed Login (Incorrect Password)
*   **Given** a registered user
*   **When** the user provides the wrong password
*   **Then** the system denies access and provides the same generic error message as in Scenario 2

### Scenario 4: Access Protected Endpoint with Valid Token
*   **Given** an authenticated user with a valid, non-expired JWT
*   **When** the user requests a protected market operation
*   **Then** the system executes the operation

### Scenario 5: Access Protected Endpoint with Invalid/Missing Token
*   **Given** an unauthenticated or unauthorized user
*   **When** the user requests a protected market operation
*   **Then** the system rejects the request

## Functional Requirements
1.  The system shall authenticate users via email and password.
2.  The system shall issue a JWT upon successful authentication.
3.  The system shall not differentiate between an unregistered email and an incorrect password in error responses.
4.  The system shall restrict all market operations (buying/selling, viewing portfolio) to authenticated users with a valid token.
5.  The system shall enforce JWT expiration.
6.  The system shall validate tokens for integrity and authenticity on each request to protected endpoints.

## Success Criteria
*   100% of authentication attempts with invalid credentials result in a standardized, non-specific error message.
*   Authentication latency for valid credentials is under 500ms.
*   Protected endpoints are inaccessible without a valid, current token.
*   All market operations correctly reject expired or malformed tokens.

## Assumptions
*   Registration logic exists and handles user persistence.
*   "Market operations" refers to buying/selling tokens and portfolio retrieval.

## Security & Privacy
*   Password verification is handled securely.
*   JWTs are signed to ensure integrity.
*   Error messages do not leak user status (registered vs. unregistered).
