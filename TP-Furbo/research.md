# Research Findings

- **Football-Data.org Integration**: Needs a dedicated adapter following the `FootballDataProvider` interface. Must handle throttling (rate limiting) and error management explicitly.
- **Data Model**: `Player` entity needs `externalId` (Football-Data ID), `teamId`, `leagueId`, and metadata. Must be extensible for WhoScored metrics.
- **Persistence**: Local cache strategy required. The system reads from local database, not the external API.
- **Sync Process**: Independent background job. No synchronous API calls during user requests.
- **Testing**: Integration tests use H2 in-memory DB and real Football-Data API (no mocks).
- **Environment**: Profile-based configuration for API Key and DB credentials.

## Decisions
- Create `com.furbo.integration.football.FootballDataProvider` interface.
- Implement `FootballDataClient` adapter with throttling logic (ScheduledExecutor or similar).
- Database: Use JPA/Hibernate mapped to `Player`, `Team`, `League` entities.
- Job: Use Spring `@Scheduled` task for the synchronization job.
- Mapper: Create `FootballDataMapper` to bridge the gap between API response and internal domain objects.

## Rationale
- Adapter pattern ensures decoupling and future-proofing for WhoScored integration.
- Local persistence strategy guarantees availability during API outages.
- External ID preservation prevents duplication during incremental updates.
