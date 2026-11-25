package com.furbo.adapter.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ToConfirmItemDTO(
    Long whoscoredPlayerId,
    String whoscoredName,
    List<String> teamNames,
    Integer age,
    Integer height,
    Integer weight,
    int totalMinutes,
    Long playerId,
    String playerName,
    String playerTeam,
    String playerPosition,
    String matchMethod,
    BigDecimal confidence,
    LocalDateTime createdAt
) {}
