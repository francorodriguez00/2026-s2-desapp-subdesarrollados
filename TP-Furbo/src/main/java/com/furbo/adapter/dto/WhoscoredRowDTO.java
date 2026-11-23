package com.furbo.adapter.dto;

public record WhoscoredRowDTO(
    String teamName,
    String tournamentName,
    String seasonName,
    int minsPlayed
) {}
