# Quickstart: Validation Guide

## Prerequisites
- PostgreSQL running locally with `furbo_db`.
- API Key from Football-Data.org.

## Setup
1. Configure environment variable: `FOOTBALL_DATA_API_KEY=...`
2. Update `application-local.yml` with credentials:
   - `spring.datasource.url: jdbc:postgresql://localhost:5432/furbo_db`
   - `spring.datasource.username: postgres`
   - `spring.datasource.password: root`

## Validation Scenarios
1. **Sync Job Trigger**: Run the batch job manually via CLI or Debugger.
   - *Expected*: Data is fetched from API and persisted in `player`, `team`, `league` tables.
2. **API Failure Resilience**: Disable network access (or simulate API down). Query the catalogue endpoint.
   - *Expected*: Catalogue returns cached data from the local database, no error propogated.
3. **Throttling Verification**: Monitor logs during sync.
   - *Expected*: Log entries show delay between requests to Football-Data API.

## Running Tests
- Use `gradle test` to execute integration tests (using H2).
