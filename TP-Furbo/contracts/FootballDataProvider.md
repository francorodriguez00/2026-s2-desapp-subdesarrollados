# Interface Contract: FootballDataProvider

This interface defines the contract for fetching data from external providers.

```java
public interface FootballDataProvider {
    List<PlayerDTO> fetchPlayersForLeague(Long leagueId);
    List<TeamDTO> fetchTeamsForLeague(Long leagueId);
    // ...
}
```

Implementations must handle:
- Throttling (Rate limiting).
- Error handling (Timeouts, 4xx/5xx).
- Mapping external API response to internal DTOs.
