package com.furbo.model.quoteStrategy;

import com.furbo.model.StrategyConfigData;
import com.furbo.service.valuation.PlayerStatsSnapshot;
import com.furbo.service.valuation.QuoteResult;

public interface ValuationStrategy {
    String code();
    QuoteResult evaluate(PlayerStatsSnapshot s, StrategyConfigData cfg);
}
