package com.furbo.config;

import com.furbo.model.StrategyConfigData;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class StrategyConfigValidator {
    public void validate(StrategyConfigData cfg) {
        if (cfg.getBaseValue() == null || cfg.getBaseValue() < 0 || 
            cfg.getScaleFactor() == null || cfg.getScaleFactor() <= 0 || 
            cfg.getMinMinutes() == null || cfg.getMinMinutes() <= 0) 
            throw new IllegalArgumentException("Invalid base config values");

        if (cfg.getRanges() != null) {
            for (Map<String, Double> ranges : cfg.getRanges().values()) {
                if (ranges.get("min") >= ranges.get("max")) throw new IllegalArgumentException("Invalid range");
            }
        }
        
        if (cfg.getWeights() != null) {
            for (Map<String, Double> weights : cfg.getWeights().values()) {
                double sum = 0.0;
                for (Double w : weights.values()) {
                    if (w != null && w > 0) sum += w;
                }
                if (Math.abs(sum - 1.0) > 0.0001) throw new IllegalArgumentException("Weights must sum 1");
            }
        }
    }
}
