package com.furbo.unit.service.valuation;

import com.furbo.model.StrategyConfigData;
import com.furbo.model.quoteStrategy.TeamContributionStrategy;
import com.furbo.service.valuation.PlayerStatsSnapshot;
import com.furbo.service.valuation.PositionGroup;
import com.furbo.service.valuation.QuoteResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AbstractWeightedStrategyTest {

    private final TeamContributionStrategy strategy = new TeamContributionStrategy();

    private PlayerStatsSnapshot snapshot(
            String position, Integer apps, Integer minutes, Double rating) {
        return new PlayerStatsSnapshot(
                1L, position, apps, minutes,
                10, 5, 2, 1, 3,
                rating, 85.0, 2.0, 1.5, 1.0, 0.5, 0.8,
                1.2, 0.9, 1.1, 3.0, 0.4, 0.2, 2.5,
                50.0, 1.0, 3.0, 0.5, null
        );
    }

    private StrategyConfigData config(
            Map<String, Map<String, Double>> weights,
            Map<String, Map<String, Double>> ranges,
            int minMinutes,
            double baseValue,
            double scaleFactor) {

        StrategyConfigData cfg = new StrategyConfigData();
        cfg.setWeights(weights);
        cfg.setRanges(ranges);
        cfg.setMinMinutes(minMinutes);
        cfg.setBaseValue(baseValue);
        cfg.setScaleFactor(scaleFactor);
        return cfg;
    }

    private Map<String, Map<String, Double>> weights(String group, String metric, double weight) {
        return Map.of(group, Map.of(metric, weight));
    }

    private Map<String, Map<String, Double>> ranges(String metric, double min, double max) {
        return Map.of(metric, Map.of("min", min, "max", max));
    }

    @Test
    void returnsNullWhenMinutesAreNull() {
        var result = strategy.evaluate(snapshot("Forward", 10, null, 8.0),
                config(weights("FW", "rating", 1.0),
                        ranges("rating", 0, 10), 900, 100, 50));

        assertNull(result);
    }

    @Test
    void returnsNullWhenMinutesAreZero() {
        var result = strategy.evaluate(snapshot("Forward", 10, 0, 8.0),
                config(weights("FW", "rating", 1.0),
                        ranges("rating", 0, 10), 900, 100, 50));

        assertNull(result);
    }

    @Test
    void returnsNullWhenAppearancesAreNull() {
        var result = strategy.evaluate(snapshot("Forward", null, 900, 8.0),
                config(weights("FW", "rating", 1.0),
                        ranges("rating", 0, 10), 900, 100, 50));

        assertNull(result);
    }

    @Test
    void returnsNullWhenAppearancesAreZero() {
        var result = strategy.evaluate(snapshot("Forward", 0, 900, 8.0),
                config(weights("FW", "rating", 1.0),
                        ranges("rating", 0, 10), 900, 100, 50));

        assertNull(result);
    }

    @Test
    void returnsNullWhenPositionIsNotRecognized() {
        var result = strategy.evaluate(snapshot("Unknown", 10, 900, 8.0),
                config(weights("FW", "rating", 1.0),
                        ranges("rating", 0, 10), 900, 100, 50));

        assertNull(result);
    }

    @Test
    void calculatesScoreAndPriceUsingPositionWeights() {
        var result = strategy.evaluate(snapshot("Forward", 10, 900, 8.0),
                config(weights("FW", "rating", 1.0),
                        ranges("rating", 0, 10), 900, 100, 50));

        assertNotNull(result);
        assertEquals(new BigDecimal("0.8000"), result.score());
        assertEquals(new BigDecimal("140.00"), result.price());
        assertEquals(PositionGroup.FW, result.group());
    }

    @Test
    void appliesReliabilityWhenMinutesAreBelowMinimum() {
        var result = strategy.evaluate(snapshot("Forward", 10, 450, 8.0),
                config(weights("FW", "rating", 1.0),
                        ranges("rating", 0, 10), 900, 100, 50));

        assertNotNull(result);
        // Score normalizado 0.8 * confiabilidad 0.5
        assertEquals(new BigDecimal("0.4000"), result.score());
        assertEquals(new BigDecimal("120.00"), result.price());
    }

    @Test
    void reliabilityDoesNotExceedOneWhenMinutesExceedMinimum() {
        var result = strategy.evaluate(snapshot("Forward", 20, 1800, 8.0),
                config(weights("FW", "rating", 1.0),
                        ranges("rating", 0, 10), 900, 100, 50));

        assertNotNull(result);
        assertEquals(new BigDecimal("0.8000"), result.score());
    }

    @Test
    void usesDefaultWeightsWhenPositionHasNoSpecificWeights() {
        Map<String, Map<String, Double>> weights = Map.of(
                "DEFAULT", Map.of("rating", 1.0)
        );

        var result = strategy.evaluate(snapshot("Forward", 10, 900, 8.0),
                config(weights, ranges("rating", 0, 10), 900, 100, 50));

        assertNotNull(result);
        assertEquals(new BigDecimal("0.8000"), result.score());
    }

    @Test
    void ignoresUnknownMetrics() {
        var result = strategy.evaluate(snapshot("Forward", 10, 900, 8.0),
                config(weights("FW", "unknownMetric", 1.0),
                        ranges("unknownMetric", 0, 10), 900, 100, 50));

        assertNotNull(result);
        assertEquals(new BigDecimal("0.0000"), result.score());
        assertEquals(new BigDecimal("100.00"), result.price());
    }

    @Test
    void ignoresMetricsWithoutConfiguredRanges() {
        var result = strategy.evaluate(snapshot("Forward", 10, 900, 8.0),
                config(weights("FW", "rating", 1.0),
                        new HashMap<>(), 900, 100, 50));

        assertNotNull(result);
        assertEquals(new BigDecimal("0.0000"), result.score());
        assertEquals(new BigDecimal("100.00"), result.price());
    }

    @Test
    void clampsTotalScoreToOne() {
        var result = strategy.evaluate(snapshot("Forward", 10, 900, 10.0),
                config(weights("FW", "rating", 2.0),
                        ranges("rating", 0, 10), 900, 100, 50));

        assertNotNull(result);
        assertEquals(new BigDecimal("1.0000"), result.score());
        assertEquals(new BigDecimal("150.00"), result.price());
    }

    @Test
    void clampsTotalScoreToZeroWhenWeightIsNegative() {
        var result = strategy.evaluate(snapshot("Forward", 10, 900, 8.0),
                config(weights("FW", "rating", -1.0),
                        ranges("rating", 0, 10), 900, 100, 50));

        assertNotNull(result);
        assertEquals(new BigDecimal("0.0000"), result.score());
        assertEquals(new BigDecimal("100.00"), result.price());
    }

    @Test
    void clampsMetricNormalizationWhenValueIsAboveMaximum() {
        var result = strategy.evaluate(snapshot("Forward", 10, 900, 15.0),
                config(weights("FW", "rating", 1.0),
                        ranges("rating", 0, 10), 900, 100, 50));

        assertNotNull(result);
        assertEquals(new BigDecimal("1.0000"), result.score());
    }

    @Test
    void clampsMetricNormalizationWhenValueIsBelowMinimum() {
        var result = strategy.evaluate(snapshot("Forward", 10, 900, -5.0),
                config(weights("FW", "rating", 1.0),
                        ranges("rating", 0, 10), 900, 100, 50));

        assertNotNull(result);
        assertEquals(new BigDecimal("0.0000"), result.score());
    }

    @Test
    void roundsPriceToTwoDecimalsAndScoreToFourDecimals() {
        var result = strategy.evaluate(snapshot("Forward", 10, 900, 1.23456),
                config(weights("FW", "rating", 1.0),
                        ranges("rating", 0, 10), 900, 100, 50));

        assertNotNull(result);
        assertEquals(new BigDecimal("0.1235"), result.score());
        assertEquals(new BigDecimal("106.17"), result.price());
    }
}