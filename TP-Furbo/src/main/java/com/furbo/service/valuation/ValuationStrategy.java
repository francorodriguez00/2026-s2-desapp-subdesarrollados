package com.furbo.service.valuation;

import com.furbo.model.StrategyConfigData;

public interface ValuationStrategy {
    String code();
    QuoteResult evaluate(PlayerStatsSnapshot s, StrategyConfigData cfg);
}
