package com.furbo.service.valuation;

import org.springframework.stereotype.Service;

@Service
public class TeamContributionStrategy extends AbstractWeightedStrategy {
    @Override
    public String code() { return "TEAM_CONTRIBUTION"; }
}
