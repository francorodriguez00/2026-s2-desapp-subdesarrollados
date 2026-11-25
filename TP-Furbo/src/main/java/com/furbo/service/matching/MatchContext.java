package com.furbo.service.matching;

import com.furbo.model.Player;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MatchContext {
    private final Map<String, List<Player>> playersByName;
    private final Map<String, List<Player>> playersByTeam;
    private final Map<Long, Player> playersById;
    private final Map<String, String> teamMap;
    private final Map<Integer, String> tournamentLeagueMap;
    private final Set<Long> linkedPlayerIds;

    public MatchContext(Map<String, List<Player>> byName, Map<String, List<Player>> byTeam, 
                        Map<Long, Player> playersById,
                        Map<String, String> teamMap, Map<Integer, String> tournamentLeagueMap,
                        Set<Long> linkedPlayerIds) {
        this.playersByName = byName;
        this.playersByTeam = byTeam;
        this.playersById = playersById;
        this.teamMap = teamMap;
        this.tournamentLeagueMap = tournamentLeagueMap;
        this.linkedPlayerIds = linkedPlayerIds;
    }

    public Map<String, List<Player>> getPlayersByName() { return playersByName; }
    public Map<String, List<Player>> getPlayersByTeam() { return playersByTeam; }
    public Map<Long, Player> getPlayersById() { return playersById; }
    public Map<String, String> getTeamMap() { return teamMap; }
    public Map<Integer, String> getTournamentLeagueMap() { return tournamentLeagueMap; }
    public Set<Long> getLinkedPlayerIds() { return linkedPlayerIds; }
}
