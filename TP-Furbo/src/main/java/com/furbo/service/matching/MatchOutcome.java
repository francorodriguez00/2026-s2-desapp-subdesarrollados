package com.furbo.service.matching;

import java.util.List;

public sealed interface MatchOutcome permits MatchOutcome.Linked, MatchOutcome.Review, MatchOutcome.Unmatched {
    record Linked(Long playerId, String method, double confidence) implements MatchOutcome {}
    record Review(List<Candidate> candidates) implements MatchOutcome {
        public record Candidate(Long playerId, double score, String reason) {}
    }
    record Unmatched() implements MatchOutcome {}
}
