package com.furbo.adapter.dto;

import java.util.List;

public record RelinkSummaryDTO(
    int alreadyLinked,
    int legacyCreated,
    int exactName,
    int exactTeam,
    int fuzzyTeam,
    int conflicts,
    int reviewPending,
    int unmatched,
    List<UnmatchedPlayerDTO> unmatchedPlayers,
    List<ConflictDTO> conflictsList
) {}
