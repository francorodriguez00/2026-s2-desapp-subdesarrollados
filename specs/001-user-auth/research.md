# Research: JWT Authentication Implementation

- **Decision**: Centralized JWT Authentication Filter.
- **Rationale**: Provides consistent, cross-cutting security enforcement, minimizing code duplication and potential security lapses in individual controllers.
- **Alternatives considered**: 
    *   Manual validation in every protected controller method (rejected: prone to human error, repetitive).
    *   AOP (Aspect-Oriented Programming) advice (considered: valid alternative, but standard interceptor/filter is more idiomatically standard for this requirement).

- **JWT Strategy**: Interface `JwtService` with implementation `JwtServiceImpl`.
    *   `generateToken(user)`
    *   `validateToken(token)`
    *   Allows for easy swapping in test environments.
