package com.furbo.service.valuation;

import java.time.LocalDateTime;

public record PlayerStatsSnapshot(
    Long playerId,
    String positionText,
    Integer apps,
    Integer minsPlayed,
    Integer goals,
    Integer assists,
    Integer yellowCards,
    Integer redCards,
    Integer manOfTheMatch,
    Double rating,
    Double passSuccess,
    Double keyPassPerGame,
    Double dribbleWonPerGame,
    Double foulGivenPerGame,
    Double dispossessedPerGame,
    Double turnoverPerGame,
    Double tacklePerGame,
    Double interceptionPerGame,
    Double foulsPerGame,
    Double clearancePerGame,
    Double wasDribbledPerGame,
    Double outfielderBlockPerGame,
    Double aerialWonPerGame,
    Double totalPassesPerGame,
    Double accurateCrossesPerGame,
    Double accurateLongPassPerGame,
    Double accurateThroughBallPerGame,
    LocalDateTime updatedAt
) {}
