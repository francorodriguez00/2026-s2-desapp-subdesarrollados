package com.furbo.config;

import com.furbo.model.StrategyConfig;
import com.furbo.model.StrategyConfigData;
import com.furbo.repository.StrategyConfigRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(2)
public class StrategySeedRunner implements CommandLineRunner {

    private final StrategyConfigRepository repository;
    private final StrategyConfigValidator validator;

    public StrategySeedRunner(StrategyConfigRepository repository, StrategyConfigValidator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    @Override
    public void run(String... args) {
        seed("CONSISTENCY_DISCIPLINE", true, createConsistencyData());
        seed("TEAM_CONTRIBUTION", false, createTeamContributionData());
    }

    private void seed(String code, boolean active, StrategyConfigData data) {
        if (repository.findTopByStrategyCodeOrderByVersionDesc(code).isEmpty()) {
            validator.validate(data);
            StrategyConfig cfg = StrategyConfig.builder()
                    .strategyCode(code)
                    .version(1)
                    .configJson(data)
                    .active(active)
                    .createdAt(LocalDateTime.now())
                    .build();
            repository.save(cfg);
        }
    }

    private StrategyConfigData createConsistencyData() {
        StrategyConfigData d = new StrategyConfigData();
        d.setBaseValue(1.0);
        d.setScaleFactor(9.0);
        d.setMinMinutes(450);
        Map<String, Map<String, Double>> ranges = new HashMap<>();
        ranges.put("rating", Map.of("min", 6.0, "max", 8.0));
        ranges.put("yellowPerGame", Map.of("min", 0.0, "max", 0.5));
        ranges.put("redPerGame", Map.of("min", 0.0, "max", 0.1));
        ranges.put("fouls", Map.of("min", 0.0, "max", 3.0));
        ranges.put("turnover", Map.of("min", 0.0, "max", 4.0));
        ranges.put("minutesShare", Map.of("min", 0.0, "max", 1.0));
        ranges.put("goalContribPer90", Map.of("min", 0.0, "max", 1.5));
        ranges.put("keyPasses", Map.of("min", 0.0, "max", 3.0));
        ranges.put("motmPerGame", Map.of("min", 0.0, "max", 0.4));
        ranges.put("passSuccess", Map.of("min", 60.0, "max", 95.0));
        ranges.put("foulsDrawn", Map.of("min", 0.0, "max", 3.0));
        d.setRanges(ranges);
        Map<String, Map<String, Double>> weights = new HashMap<>();
        Map<String, Double> defaultWeights = new HashMap<>();
        defaultWeights.put("rating", 0.35);
        defaultWeights.put("minutesShare", 0.20);
        defaultWeights.put("goalContribPer90", 0.15);
        defaultWeights.put("keyPasses", 0.10);
        defaultWeights.put("motmPerGame", 0.10);
        defaultWeights.put("passSuccess", 0.05);
        defaultWeights.put("foulsDrawn", 0.05);
        defaultWeights.put("yellowPerGame", -0.08);
        defaultWeights.put("redPerGame", -0.20);
        defaultWeights.put("fouls", -0.05);
        defaultWeights.put("turnover", -0.07);
        weights.put("DEFAULT", defaultWeights);
        d.setWeights(weights);
        return d;
    }

    private StrategyConfigData createTeamContributionData() {
        StrategyConfigData d = new StrategyConfigData();
        d.setBaseValue(1.0);
        d.setScaleFactor(9.0);
        d.setMinMinutes(450);
        Map<String, Map<String, Double>> ranges = new HashMap<>();
        ranges.put("keyPasses", Map.of("min", 0.0, "max", 3.0));
        ranges.put("throughBalls", Map.of("min", 0.0, "max", 1.0));
        ranges.put("crosses", Map.of("min", 0.0, "max", 2.0));
        ranges.put("longPasses", Map.of("min", 0.0, "max", 6.0));
        ranges.put("totalPasses", Map.of("min", 10.0, "max", 80.0));
        ranges.put("passSuccess", Map.of("min", 60.0, "max", 95.0));
        ranges.put("dribbles", Map.of("min", 0.0, "max", 3.5));
        ranges.put("tackles", Map.of("min", 0.0, "max", 4.0));
        ranges.put("interceptions", Map.of("min", 0.0, "max", 3.0));
        ranges.put("clearances", Map.of("min", 0.0, "max", 6.0));
        ranges.put("blocks", Map.of("min", 0.0, "max", 1.5));
        ranges.put("aerialWon", Map.of("min", 0.0, "max", 5.0));
        ranges.put("rating", Map.of("min", 6.0, "max", 8.0));
        ranges.put("minutesShare", Map.of("min", 0.0, "max", 1.0));
        ranges.put("dispossessed", Map.of("min", 0.0, "max", 3.0));
        ranges.put("wasDribbled", Map.of("min", 0.0, "max", 2.0));
        d.setRanges(ranges);
        Map<String, Map<String, Double>> weights = new HashMap<>();
        Map<String, Double> fwWeights = new HashMap<>();
        fwWeights.put("keyPasses", 0.25); fwWeights.put("throughBalls", 0.10); fwWeights.put("crosses", 0.10); 
        fwWeights.put("totalPasses", 0.05); fwWeights.put("passSuccess", 0.05); fwWeights.put("dribbles", 0.20); 
        fwWeights.put("tackles", 0.05); fwWeights.put("rating", 0.20); fwWeights.put("dispossessed", -0.10); fwWeights.put("wasDribbled", -0.05);
        weights.put("FW", fwWeights);
        Map<String, Double> mfWeights = new HashMap<>();
        mfWeights.put("keyPasses", 0.15); mfWeights.put("throughBalls", 0.10); mfWeights.put("longPasses", 0.10); 
        mfWeights.put("totalPasses", 0.10); mfWeights.put("passSuccess", 0.10); mfWeights.put("dribbles", 0.05); 
        mfWeights.put("tackles", 0.10); mfWeights.put("interceptions", 0.10); mfWeights.put("rating", 0.20); mfWeights.put("dispossessed", -0.10); mfWeights.put("wasDribbled", -0.10);
        weights.put("MF", mfWeights);
        Map<String, Double> dfWeights = new HashMap<>();
        dfWeights.put("longPasses", 0.10); dfWeights.put("passSuccess", 0.10); dfWeights.put("tackles", 0.15); 
        dfWeights.put("interceptions", 0.15); dfWeights.put("clearances", 0.10); dfWeights.put("blocks", 0.10); 
        dfWeights.put("aerialWon", 0.10); dfWeights.put("rating", 0.20); dfWeights.put("dispossessed", -0.05); dfWeights.put("wasDribbled", -0.15);
        weights.put("DF", dfWeights);
        Map<String, Double> gkWeights = new HashMap<>();
        gkWeights.put("passSuccess", 0.20); gkWeights.put("clearances", 0.10); gkWeights.put("aerialWon", 0.10); 
        gkWeights.put("rating", 0.50); gkWeights.put("minutesShare", 0.10);
        weights.put("GK", gkWeights);
        return d;
    }
}
