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
        if (playerRepository.count() == 0) {
            fetchAndSavePlayers();
        }
        return playerRepository.findAll();
    }

    private void fetchAndSavePlayers() {
        List<Player> allPlayers = new ArrayList<>();
        
        for (String leagueCode : LEAGUES) {
            try {
                CompetitionTeamsDTO competition = restTemplate.getForObject(
                        API_URL + "/competitions/" + leagueCode + "/teams", CompetitionTeamsDTO.class);
                
                if (competition != null && competition.getTeams() != null) {
                    // Procesar equipos (lotes limitados para no exceder rate limits)
                    for (int i = 0; i < competition.getTeams().size(); i++) {
                        // Sleep breve cada 5 equipos
                        if (i > 0 && i % 5 == 0) Thread.sleep(6000);
                        
                        var team = competition.getTeams().get(i);
                        TeamDetailDTO teamDetail = restTemplate.getForObject(
                                API_URL + "/teams/" + team.getId(), TeamDetailDTO.class);
                        
                        if (teamDetail != null && teamDetail.getSquad() != null) {
                            for (PlayerDTO p : teamDetail.getSquad()) {
                                allPlayers.add(Player.builder()
                                        .name(p.getName())
                                        .position(p.getPosition())
                                        .team(teamDetail.getName())
                                        .nationality(p.getNationality())
                                        .league(leagueCode)
                                        .build());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // Loguear error pero continuar con otras ligas
                System.err.println("Error fetching league " + leagueCode + ": " + e.getMessage());
            }
        }
        playerRepository.saveAll(allPlayers);
    }
}
