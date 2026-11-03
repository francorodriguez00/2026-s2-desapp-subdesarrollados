package com.furbo.service.matching;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LinkOutcome {
    public enum Type {
        ALREADY_LINKED, LEGACY_CREATED, EXACT_NAME, EXACT_TEAM, FUZZY_TEAM, REVIEW, UNMATCHED, CONFLICT
    }
    private final Long playerId;
    private final Type outcome;
    private final String method;
    private final Double confidence;
    private final Long disputedPlayerId;
    private final String externalId;
}
