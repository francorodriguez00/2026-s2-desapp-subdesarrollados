package com.furbo.integration.football;

import com.furbo.integration.football.dto.PlayerDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.beans.factory.annotation.Value;

@Component
public class FootballDataClient implements FootballDataProvider {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String baseUrl = "https://api.football-data.org/v4";

    public FootballDataClient(RestTemplate restTemplate, @Value("${football-data.api-key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
    }

    // Método de ejemplo para fetch
    public PlayerDTO[] getPlayersForLeague(String leagueCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        // Simulación de Throttling simple (debería ser más robusto en producción)
        try { Thread.sleep(1000); } catch (InterruptedException e) {}

        return restTemplate.exchange(
            baseUrl + "/competitions/" + leagueCode + "/teams", 
            HttpMethod.GET, 
            entity, 
            PlayerDTO[].class
        ).getBody();
    }
}
