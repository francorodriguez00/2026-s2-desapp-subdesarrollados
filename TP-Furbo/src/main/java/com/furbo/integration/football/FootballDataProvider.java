package com.furbo.integration.football;

import com.furbo.integration.football.dto.PlayerDTO;
import java.util.List;

public interface FootballDataProvider {
    List<PlayerDTO> getPlayersForLeague(String leagueCode);
}