package com.furbo.service;

import com.furbo.model.Player;
import com.furbo.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PlayerMatcherService {
    private final PlayerRepository playerRepository;

    public PlayerMatcherService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public Optional<Player> findMatch(String name, String team) {
        String normalizedName = normalize(name);
        // Lista optimizada buscando coincidencias exactas primero
        List<Player> candidates = playerRepository.findAll().stream()
            .filter(p -> normalize(p.getName()).equals(normalizedName))
            .collect(Collectors.toList());
        
        if (candidates.size() == 1) {
            return Optional.of(candidates.get(0));
        }
        return Optional.empty();
    }

    private String normalize(String input) {
        if (input == null) return "";
        return input.toLowerCase().replaceAll("[^a-z0-9]", "");
    }
}
