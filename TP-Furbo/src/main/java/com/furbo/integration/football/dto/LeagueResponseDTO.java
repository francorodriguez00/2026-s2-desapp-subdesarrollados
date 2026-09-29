package com.furbo.integration.football.dto;

import java.util.List;

public class LeagueResponseDTO {
    private List<TeamDTO> teams;

    public List<TeamDTO> getTeams() { return teams; }
    public void setTeams(List<TeamDTO> teams) { this.teams = teams; }
}