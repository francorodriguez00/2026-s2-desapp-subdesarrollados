package com.furbo.model.quoteStrategy;

import org.springframework.stereotype.Service;

@Service
public class TeamContributionStrategy extends AbstractWeightedStrategy {
    @Override
    public String code() { return "TEAM_CONTRIBUTION"; }
}
