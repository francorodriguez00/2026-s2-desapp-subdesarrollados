# Data Model

## Entities

### Player
- `id` (Long, PK)
- `externalId` (Long, unique, indexed, mapping to Football-Data ID)
- `name` (String)
- `team` (ManyToOne relationship to `Team`)
- `league` (ManyToOne relationship to `League`)
- `position` (String)
- `nationality` (String)
- `metrics` (Embedded or Json, prepared for future WhoScored integration)

### Team
- `id` (Long, PK)
- `externalId` (Long, unique)
- `name` (String)
- `league` (ManyToOne)

### League
- `id` (Long, PK)
- `externalId` (Long, unique)
- `name` (String)

## Validation Rules
- `externalId`: Must be unique and present for external synchronization.
- `name`: NotNull.

## Extensibility Plan
- The `Player` entity is designed with an extensible `metrics` field (or nullable fields) to support future WhoScored performance metrics without requiring schema refactoring.
