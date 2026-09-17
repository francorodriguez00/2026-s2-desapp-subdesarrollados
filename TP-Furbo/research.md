# Research Findings

- **Hashing Implementation**: No existing hashing infrastructure found. Will design an interface `PasswordHasher` with an implementation `BCryptPasswordHasher` for production and `PlaintextPasswordHasher` for testing if needed.
- **Project Structure**: Clean architecture established. Will create `User` and `Portfolio` models, `UserRepository` and `PortfolioRepository`, `UserService` to handle registration orchestrating both.
- **Database**: PostgreSQL (provided via local profile).
- **Testcontainers**: Required for integration tests.

## Decisions
- Create `com.furbo.security.PasswordHasher` interface.
- Implement `BCryptPasswordHasher` in `com.furbo.security`.
- Register as Bean in `com.furbo.config`.
- Automate Portfolio creation in `UserService` (transactional).

## Rationale
- Decoupling hashing strategy allows easier testing and swapping of algorithms.
- Transactional service ensures consistency between User creation and Portfolio initialization.
