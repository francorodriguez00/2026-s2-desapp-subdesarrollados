package com.furbo.integration.football;

import com.furbo.domain.model.Player;
import com.furbo.integration.football.dto.PlayerDTO;
import org.springframework.stereotype.Component;

@Component
public class FootballDataMapper {
    public Player toDomain(PlayerDTO dto) {
        return Player.builder()
                .externalId(dto.getId())
                .name(dto.getName())
                .position(dto.getPosition())
                .nationality(dto.getNationality())
                .build();
    }
}
