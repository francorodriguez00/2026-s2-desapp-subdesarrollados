package com.furbo.adapter.dto;

public record UnmatchedPlayerDTO(
    Long wsId,
    String name,
    String teamName,
    String tournamentName,
    String pos,
    int totalMinutes
) {
    public int totalMins() { return totalMinutes; }
    public int getTotalMinutes() { return totalMinutes; }
}
