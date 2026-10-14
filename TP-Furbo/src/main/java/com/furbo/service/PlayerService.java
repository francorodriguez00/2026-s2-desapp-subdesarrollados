package com.furbo.service;

import com.furbo.adapter.dto.external.CompetitionTeamsDTO;
import com.furbo.adapter.dto.external.PlayerDTO;
import com.furbo.adapter.dto.external.TeamDetailDTO;
import com.furbo.model.Player;
import com.furbo.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final RestTemplate restTemplate;
    private static final String API_URL = "https://api.football-data.org/v4";
    private static final String[] LEAGUES = {"PL", "PD", "SA", "BL1", "FL1"};

    public PlayerService(PlayerRepository playerRepository, RestTemplate restTemplate) {
        this.playerRepository = playerRepository;
        this.restTemplate = restTemplate;
    }

    public List<Player> getPlayers() {
        return playerRepository.findAll();
    }

    public void fetchAndSavePlayers() {
        for (String leagueCode : LEAGUES) {
            try {
                CompetitionTeamsDTO competition = fetch(API_URL + "/competitions/" + leagueCode + "/teams", CompetitionTeamsDTO.class);
                
                if (competition != null && competition.getTeams() != null) {
                    List<Player> playersOfLeague = new ArrayList<>();
                    for (var team : competition.getTeams()) {
                        TeamDetailDTO teamDetail = fetch(API_URL + "/teams/" + team.getId(), TeamDetailDTO.class);
                        
                        if (teamDetail != null && teamDetail.getSquad() != null) {
                            for (PlayerDTO p : teamDetail.getSquad()) {
                                playersOfLeague.add(Player.builder()
                                        .name(p.getName())
                                        .position(p.getPosition())
                                        .team(teamDetail.getName())
                                        .nationality(p.getNationality())
                                        .league(leagueCode)
                                        .build());
                            }
                        }
                    }
                    playerRepository.saveAll(playersOfLeague);
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
