package com.furbo.adapter.dto;

public record ConflictDTO(
    String whoscoredId,
    String name,
    String teamName,
    int minutes,
    Long disputedPlayerId,
    String ownerExternalId
) {}
