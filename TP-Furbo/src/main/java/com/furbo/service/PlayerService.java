package com.furbo.service;

import com.furbo.adapter.dto.external.CompetitionTeamsDTO;
import com.furbo.adapter.dto.external.PlayerDTO;
import com.furbo.adapter.dto.external.TeamDetailDTO;
import com.furbo.exceptions.InvalidFilterException;
import com.furbo.model.Player;
import com.furbo.model.enums.League;
import com.furbo.model.enums.PlayerPosition;
import com.furbo.repository.PlayerRepository;
import com.furbo.repository.specs.PlayerSpecs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final RestTemplate restTemplate;
    private final com.furbo.service.SuperuserTokenService tokenService;
    private static final String API_URL = "https://api.football-data.org/v4";
    private static final java.util.Set<String> SORTABLE = java.util.Set.of("id", "name", "team", "league", "position", "nationality");

    public PlayerService(PlayerRepository playerRepository, RestTemplate restTemplate, com.furbo.service.SuperuserTokenService tokenService) {
        this.playerRepository = playerRepository;
        this.restTemplate = restTemplate;
        this.tokenService = tokenService;
    }

    private void upsertPlayers(String leagueCode, List<PlayerSnapshot> incoming) {
        java.util.Map<Long, PlayerSnapshot> byExternalId = new java.util.LinkedHashMap<>();
        incoming.forEach(s -> byExternalId.put(s.externalId(), s));

        java.util.Map<Long, Player> existing = playerRepository
                .findByExternalIdIn(byExternalId.keySet()).stream()
                .collect(java.util.stream.Collectors.toMap(Player::getExternalId, java.util.function.Function.identity()));

        List<Player> toSave = new ArrayList<>();
        for (PlayerSnapshot s : byExternalId.values()) {
            Player p = existing.getOrDefault(s.externalId(), new Player());
            p.setExternalId(s.externalId());
            p.setName(s.name());
            p.setPosition(s.position());
            p.setNationality(s.nationality());
            p.setTeam(s.team());
            p.setLeague(leagueCode);
            toSave.add(p);
        }
        playerRepository.saveAll(toSave);
        toSave.forEach(tokenService::asignarTokens);
    }

    private record PlayerSnapshot(Long externalId, String name, String position, String nationality, String team) {}

    public Page<Player> getPlayers(String league, String team, String position, Pageable pageable) {
        String leagueCode = null;
        if (StringUtils.hasText(league)) {
            leagueCode = League.parse(league)
                    .orElseThrow(() -> new InvalidFilterException("La liga solicitada no existe"))
                    .name();
        }

        String positionValue = null;
        if (StringUtils.hasText(position)) {
            positionValue = PlayerPosition.parse(position)
                    .orElseThrow(() -> new InvalidFilterException("La posición solicitada no existe"))
                    .apiValue();
        }

        String teamValue = StringUtils.hasText(team) ? team.trim() : null;

        pageable.getSort().forEach(order -> {
            if (!SORTABLE.contains(order.getProperty()))
                throw new InvalidFilterException("Campo de orden no válido: " + order.getProperty());
        });

        return playerRepository.findAll(
                PlayerSpecs.withFilters(leagueCode, teamValue, positionValue), pageable);
    }

    public void fetchAndSavePlayers() {
        for (League league : League.values()) {
            String leagueCode = league.name();
            try {
                CompetitionTeamsDTO competition = fetch(API_URL + "/competitions/" + leagueCode + "/teams", CompetitionTeamsDTO.class);
                
                if (competition != null && competition.getTeams() != null) {
                    List<PlayerSnapshot> snapshots = new ArrayList<>();
                    for (var team : competition.getTeams()) {
                        TeamDetailDTO teamDetail = fetch(API_URL + "/teams/" + team.getId(), TeamDetailDTO.class);
                        
                        if (teamDetail != null && teamDetail.getSquad() != null) {
                            for (PlayerDTO p : teamDetail.getSquad()) {
                                if (p.getId() != null) {
                                    snapshots.add(new PlayerSnapshot(p.getId(), p.getName(), p.getPosition(), p.getNationality(), teamDetail.getName()));
                                }
                            }
                        }
                    }
                    upsertPlayers(leagueCode, snapshots);
                }
            } catch (Exception e) {
                System.err.println("Error fetching league " + leagueCode + ": " + e.getMessage());
            }
        }
    }

    private <T> T fetch(String url, Class<T> type) {
        for (int attempt = 0; attempt < 3; attempt++) {
            sleepQuietly(6500); 
            try {
                return restTemplate.getForObject(url, type);
            } catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests e) {
                sleepQuietly(45_000); 
            }
        }
        throw new IllegalStateException("Rate limit persistente en " + url);
    }

    private void sleepQuietly(long ms) {
        try { Thread.sleep(ms); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new RuntimeException(e); }
    }
}
