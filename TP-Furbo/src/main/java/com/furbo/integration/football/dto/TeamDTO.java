package com.furbo.integration.football.dto;

import java.util.List;

public class TeamDTO {
    private List<PlayerDTO> squad;

    public List<PlayerDTO> getSquad() { return squad; }
    public void setSquad(List<PlayerDTO> squad) { this.squad = squad; }
}