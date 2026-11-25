package com.furbo.adapter.dto;

import java.util.List;

public record UnmatchedCandidatesResponseDTO(
    WhoscoredPlayerSummaryDTO player,
    List<UnmatchedCandidateItemDTO> candidates
) {}
