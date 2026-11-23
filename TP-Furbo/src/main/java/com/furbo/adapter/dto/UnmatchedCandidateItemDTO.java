package com.furbo.adapter.dto;

public record UnmatchedCandidateItemDTO(
    Long playerId,
    String name,
    String team,
    String position,
    String league,
    String nationality,
    Double score,
    String rule
) {}
