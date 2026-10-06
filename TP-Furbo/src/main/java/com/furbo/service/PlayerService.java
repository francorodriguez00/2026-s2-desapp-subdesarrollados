package com.furbo.service;

import com.furbo.model.Player;
import com.furbo.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final RestTemplate restTemplate;

    @Value("${football-data.api-key}")
    private String apiKey;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
        this.restTemplate = new RestTemplate();
    }

    public List<Player> getPlayers() {
        if (playerRepository.count() == 0) {
            // Aquí iría la lógica para consumir la API y guardar en BD
            // Por ahora, retornamos vacío o simulamos datos si es necesario
        }
        return playerRepository.findAll();
    }
}
