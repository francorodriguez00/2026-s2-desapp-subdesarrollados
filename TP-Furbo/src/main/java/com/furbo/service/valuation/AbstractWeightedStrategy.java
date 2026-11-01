package com.furbo.service.valuation;

import com.furbo.model.StrategyConfigData;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractWeightedStrategy implements ValuationStrategy {

    @Override
    public QuoteResult evaluate(PlayerStatsSnapshot s, StrategyConfigData cfg) {
        if (s.minsPlayed() == null || s.minsPlayed() == 0 || s.apps() == null || s.apps() == 0) return null;
        
        PositionGroup group = PositionGroup.fromPositionText(s.positionText());
        if (group == null) return null;

        double reliability = Math.min(1.0, (s.minsPlayed() != null ? s.minsPlayed() : 0.0) / (double) cfg.getMinMinutes());
        
        Map<String, Double> weights = cfg.getWeights() != null 
            ? cfg.getWeights().getOrDefault(group.name(), cfg.getWeights().get("DEFAULT"))
            : new HashMap<>();
        
        double totalScore = 0.0;

        for (Map.Entry<String, Double> entry : weights.entrySet()) {
            String metricCode = entry.getKey();
            Double weight = entry.getValue();
            
            Metric m = getMetricByCode(metricCode);
            if (m == null) continue;
            
            double val = m.extract(s);
            Map<String, Double> range = cfg.getRanges() != null ? cfg.getRanges().get(metricCode) : null;
            if (range == null) continue;
            
            double min = range.get("min");
            double max = range.get("max");
            double norm = Math.max(0.0, Math.min(1.0, (val - min) / (max - min)));
            
            totalScore += weight * norm;
        }

        totalScore = Math.max(0.0, Math.min(1.0, totalScore)) * reliability;
        BigDecimal price = BigDecimal.valueOf(cfg.getBaseValue())
                .add(BigDecimal.valueOf(totalScore).multiply(BigDecimal.valueOf(cfg.getScaleFactor())))
                .setScale(2, RoundingMode.HALF_UP);

        return new QuoteResult(BigDecimal.valueOf(totalScore).setScale(4, RoundingMode.HALF_UP), price, group);
    }

    private Metric getMetricByCode(String code) {
        for (Metric m : Metric.values()) {
            if (m.getCode().equals(code)) return m;
        }
        return null;
    }
}
