# Tasks: Jugadores Mercado

**Input**: Design documents from `/specs/001-user-auth/` (re-utilized)

**Organization**: Tasks are grouped by logical components to enable independent implementation and testing.

## Phase 1: Setup
- [X] T001 Verify project structure per implementation plan
- [X] T002 Configure local profile properties for API Key and DB in src/main/resources/application-local.yml

---

## Phase 2: Foundational (Blocking Prerequisites)
- [X] T003 Setup database schema for Players, Teams, and Leagues in src/main/resources/db/migration/V1__init_schema.sql
- [X] T004 Define `FootballDataProvider` interface in src/main/java/com/furbo/integration/football/FootballDataProvider.java
- [X] T005 Implement `FootballDataClient` adapter with throttling in src/main/java/com/furbo/integration/football/FootballDataClient.java
- [X] T006 Implement `FootballDataMapper` in src/main/java/com/furbo/integration/football/FootballDataMapper.java

---

## Phase 3: User Story 1 - Sincronización y Caché (Priority: P1)
**Goal**: Implement the batch process to fetch and persist data from Football-Data.org.

- [X] T007 [P] [US1] Create Player entity in src/main/java/com/furbo/domain/model/Player.java
- [X] T008 [P] [US1] Create Team entity in src/main/java/com/furbo/domain/model/Team.java
- [X] T009 [P] [US1] Create League entity in src/main/java/com/furbo/domain/model/League.java
- [X] T010 [US1] Create Spring Scheduler Job in src/main/java/com/furbo/job/SyncCatalogJob.java
- [X] T011 [US1] Implement persistence logic for synchronized data
- [X] T012 [P] [US2] Create PlayerRepository in src/main/java/com/furbo/repository/PlayerRepository.java
- [X] T013 [US2] Implement PlayerService in src/main/java/com/furbo/service/PlayerService.java
- [X] T014 [US2] Implement PlayerController in src/main/java/com/furbo/controller/PlayerController.java

---

## Phase 5: Polish & Cross-Cutting Concerns
- [X] T015 [P] Add integration tests using H2 in src/test/java/com/furbo/integration/CatalogIntegrationTest.java
- [X] T016 Run quickstart.md validation scenarios

---

## Dependencies & Execution Order
- Phase 1: Setup (No dependencies)
- Phase 2: Foundational (Depends on Phase 1)
- Phase 3: Sincronización (Depends on Phase 2)
- Phase 4: Consulta (Depends on Phase 3)
- Phase 5: Polish (Depends on all)

## Parallel Opportunities
- Phase 3/4 tasks marked [P] can run in parallel if independent of specific persistence implementations.

## Implementation Strategy
- Follow the MVP plan starting with Synchronization, then exposing the Catalogue via API.
