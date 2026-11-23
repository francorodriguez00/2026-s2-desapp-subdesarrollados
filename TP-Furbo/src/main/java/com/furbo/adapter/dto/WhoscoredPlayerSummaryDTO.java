package com.furbo.adapter.dto;

import java.util.List;

public record WhoscoredPlayerSummaryDTO(
    Long whoscoredPlayerId,
    String name,
    List<String> teamNames,
    String tournamentName,
    String positionText,
    Integer age,
    Integer height,
    Integer weight,
    int totalMinutes,
    List<WhoscoredRowDTO> rows
) {}
