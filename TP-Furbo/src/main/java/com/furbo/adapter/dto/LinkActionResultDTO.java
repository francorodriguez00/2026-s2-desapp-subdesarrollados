package com.furbo.adapter.dto;

public record LinkActionResultDTO(
    String externalId,
    String action,
    Long playerId,
    Long previousPlayerId,
    int rowsUpdated
) {}
