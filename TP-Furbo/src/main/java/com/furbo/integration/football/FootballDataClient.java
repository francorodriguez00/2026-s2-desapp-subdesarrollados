package com.furbo.integration.football;

import com.furbo.integration.football.dto.LeagueResponseDTO;
import com.furbo.integration.football.dto.PlayerDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FootballDataClient implements FootballDataProvider {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String baseUrl = "https://api.football-data.org/v4";

    public FootballDataClient(RestTemplate restTemplate, @Value("${football-data.api-key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
    }

    @Override
    public List<PlayerDTO> getPlayersForLeague(String leagueCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<LeagueResponseDTO> response = restTemplate.exchange(
                    baseUrl + "/competitions/" + leagueCode + "/teams",
                    HttpMethod.GET,
                    entity,
                    LeagueResponseDTO.class
            );

            LeagueResponseDTO body = response.getBody();

            if (body != null && body.getTeams() != null) {
                // Navegamos por los equipos y juntamos todos los jugadores en una sola lista
                return body.getTeams().stream()
                        .filter(team -> team.getSquad() != null)
                        .flatMap(team -> team.getSquad().stream())
                        .collect(Collectors.toList());
            }

        } catch (Exception e) {
            System.err.println("Error en liga " + leagueCode + ": " + e.getMessage());
        }

        return Collections.emptyList();
    }
}