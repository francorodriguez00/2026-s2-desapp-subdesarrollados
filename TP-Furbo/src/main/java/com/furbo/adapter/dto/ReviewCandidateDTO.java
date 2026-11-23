package com.furbo.adapter.dto;

public record ReviewCandidateDTO(
    Long playerId,
    String name,
    String team,
    String position,
    String league,
    String nationality,
    double score,
    String reason,
    String linkedToWhoscoredId
) {}
