# Data Model

The existing `User` entity is the primary entity for authentication.

### Entity: User (Existing)
*   **id**: Unique identifier.
*   **email**: String, unique, used for authentication.
*   **password_hash**: String, hashed password stored during registration.

### Token Handling
*   **Not persisted in the database.**
*   Handled via JWT claims:
    *   **subject**: `email`
    *   **exp**: `timestamp` (expiration time)
