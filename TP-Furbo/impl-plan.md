# Implementation Plan: User Registration

## Technical Context
- **Stack**: Java 21, Spring Boot 3, PostgreSQL, Spring Security, JWT (stateless)
- **Database**: PostgreSQL (jdbc:postgresql://localhost:5432/tp-furbo)
- **Authentication**: JWT, Hashing with interchangeable interface strategy
- **Requirements**:
  - Controller: `UserController` (Registration, Query, etc.)
  - Email Validation: Unique constraint in DB, validation before persistence
  - Password: Hashed, never in plain text
  - Portfolio: Initialized empty automatically on registration

## Constitution Check
- **I. Arquitectura en capas**: Followed (Controller -> Service -> Model -> Repository)
- **II. Modelo rico**: Entities will contain business logic.
- **III. Validaciones en niveles**: DTOs, Service, Model (Domain exceptions)
- **IV. Estrategia de Testing**: Unit tests (domain), Integration (Testcontainers)
- **V. Definición de Terminado**: Compliance required.
- **VI. Idioma y Nomenclatura**: Spanish documentation, English identifiers.
- **VII. Observabilidad**: Standard logging.
- **VIII. YAGNI**: Monolith modular.

## Gate Evaluations
- [ ] Database credentials: Hardcoded for local (acceptable per request)
- [ ] Testcontainers usage: Required for tests.

## Research Needed
- NEEDS CLARIFICATION: How is the Hashing interface currently implemented? (If at all)
- NEEDS CLARIFICATION: Project structure for UserPortfolio creation logic.

## Tasks
- Phase 0: Research (Resolve clarifications)
- Phase 1: Design (data-model.md, contracts, quickstart.md)
