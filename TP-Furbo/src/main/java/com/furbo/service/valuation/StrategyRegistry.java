package com.furbo.service.valuation;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class StrategyRegistry {
    private final Map<String, ValuationStrategy> strategies;

    public StrategyRegistry(List<ValuationStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(ValuationStrategy::code, Function.identity()));
    }

    public ValuationStrategy get(String code) {
        return strategies.get(code);
    }
}
