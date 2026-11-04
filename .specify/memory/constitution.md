<!-- 
Sync Impact Report
Version change: [OLD_VERSION] → 0.1.0
Modified principles: None
Added sections: Source of Truth (Governance)
Removed sections: None
Follow-up TODOs: Ratification Date and Project Name need to be finalized.
-->

# Subdesarrollados Constitution

## Core Principles

### I. Library-First
Every feature starts as a standalone library; Libraries must be self-contained, independently testable, documented; Clear purpose required - no organizational-only libraries.

### II. CLI Interface
Every library exposes functionality via CLI; Text in/out protocol: stdin/args → stdout, errors → stderr; Support JSON + human-readable formats.

### III. Test-First (NON-NEGOTIABLE)
TDD mandatory: Tests written → User approved → Tests fail → Then implement; Red-Green-Refactor cycle strictly enforced.

### IV. Integration Testing
Focus areas requiring integration tests: New library contract tests, Contract changes, Inter-service communication, Shared schemas.

### V. Simplicity and Observability
Text I/O ensures debuggability; Structured logging required; Start simple, YAGNI principles.

## Project Constraints

Development follows project-specific constraints, emphasizing modularity, testability, and standard communication protocols as defined in the core principles.

## Development Workflow

Development proceeds with a strict adherence to the Red-Green-Refactor cycle, utilizing integration testing for all contract-breaking changes and shared schema updates.

## Governance

The constitution supersedes all other practices; Amendments require documentation, approval, and a migration plan. All PRs/reviews must verify compliance. 

The source of truth for all decisions regarding the project is located at the root of the project in the `recursos` folder: `2026.2doSem.DocumentoDeVisión.md`. This document is the foundation that governs the project's path.

**Version**: 0.1.0 | **Ratified**: TODO(RatificationDate) | **Last Amended**: 2026-09-29
