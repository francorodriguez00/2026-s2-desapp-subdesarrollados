package com.furbo.unit.service.valuation;

import com.furbo.service.valuation.Metric;
import com.furbo.service.valuation.PlayerStatsSnapshot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MetricTest {

    private PlayerStatsSnapshot snapshot() {
        return new PlayerStatsSnapshot(
                1L, "Forward", 10, 900,
                10, 5, 4, 2, 3,
                8.5, 87.5, 2.1, 1.2, 1.3, 0.4, 0.5,
                1.1, 0.9, 1.4, 2.2, 0.3, 0.6, 2.5,
                50.0, 1.5, 3.5, 0.7, null
        );
    }

    @Test
    void extractsGoalContributionsPer90() {
        // (10 goles + 5 asistencias) / (900 / 90)
        assertEquals(1.5, Metric.GOAL_CONTRIB_PER_90.extract(snapshot()), 0.0001);
    }

    @Test
    void extractsKeyPasses() {
        assertEquals(2.1, Metric.KEY_PASSES.extract(snapshot()), 0.0001);
    }

    @Test
    void extractsThroughBalls() {
        assertEquals(0.7, Metric.THROUGH_BALLS.extract(snapshot()), 0.0001);
    }

    @Test
    void extractsLongPasses() {
        assertEquals(3.5, Metric.LONG_PASSES.extract(snapshot()), 0.0001);
    }

    @Test
    void extractsCrosses() {
        assertEquals(1.5, Metric.CROSSES.extract(snapshot()), 0.0001);
    }

    @Test
    void extractsTotalPasses() {
        assertEquals(50.0, Metric.TOTAL_PASSES.extract(snapshot()), 0.0001);
    }

    @Test
    void extractsPassSuccess() {
        assertEquals(87.5, Metric.PASS_SUCCESS.extract(snapshot()), 0.0001);
    }

    @Test
    void extractsDribbles() {
        assertEquals(1.2, Metric.DRIBBLES.extract(snapshot()), 0.0001);
    }

    @Test
    void extractsTackles() {
        assertEquals(1.1, Metric.TACKLES.extract(snapshot()), 0.0001);
    }

    @Test
    void extractsInterceptions() {
        assertEquals(0.9, Metric.INTERCEPTIONS.extract(snapshot()), 0.0001);
    }

    @Test
    void extractsClearances() {
        assertEquals(2.2, Metric.CLEARANCES.extract(snapshot()), 0.0001);
    }

    @Test
    void extractsBlocks() {
        assertEquals(0.6, Metric.BLOCKS.extract(snapshot()), 0.0001);
    }

    @Test
    void extractsAerialWon() {
        assertEquals(2.5, Metric.AERIAL_WON.extract(snapshot()), 0.0001);
    }

    @Test
    void extractsRating() {
        assertEquals(8.5, Metric.RATING.extract(snapshot()), 0.0001);
    }

    @Test
    void calculatesMinutesShare() {
        assertEquals(1.0, Metric.MINUTES_SHARE.extract(snapshot()), 0.0001);
    }

    @Test
    void calculatesMotmPerGame() {
        assertEquals(0.3, Metric.MOTM_PER_GAME.extract(snapshot()), 0.0001);
    }

    @Test
    void extractsFoulsDrawn() {
        assertEquals(1.3, Metric.FOULS_DRAWN.extract(snapshot()), 0.0001);
    }

    @Test
    void calculatesYellowCardsPerGame() {
        assertEquals(0.4, Metric.YELLOW_PER_GAME.extract(snapshot()), 0.0001);
    }

    @Test
    void calculatesRedCardsPerGame() {
        assertEquals(0.2, Metric.RED_PER_GAME.extract(snapshot()), 0.0001);
    }

    @Test
    void extractsFouls() {
        assertEquals(1.4, Metric.FOULS.extract(snapshot()), 0.0001);
    }

    @Test
    void extractsTurnovers() {
        assertEquals(0.5, Metric.TURNOVER.extract(snapshot()), 0.0001);
    }

    @Test
    void extractsDispossessed() {
        assertEquals(0.4, Metric.DISPOSSESSED.extract(snapshot()), 0.0001);
    }

    @Test
    void extractsWasDribbled() {
        assertEquals(0.3, Metric.WAS_DRIBBLED.extract(snapshot()), 0.0001);
    }

    @Test
    void returnsZeroForNullMetricValues() {
        PlayerStatsSnapshot s = new PlayerStatsSnapshot(
                1L, "Forward", 10, 900,
                null, null, null, null, null,
                null, null, null, null, null, null, null,
                null, null, null, null, null, null, null,
                null, null, null, null, null
        );

        assertEquals(0.0, Metric.KEY_PASSES.extract(s), 0.0001);
        assertEquals(0.0, Metric.RATING.extract(s), 0.0001);
        assertEquals(0.0, Metric.GOAL_CONTRIB_PER_90.extract(s), 0.0001);
        assertEquals(0.0, Metric.MOTM_PER_GAME.extract(s), 0.0001);
        assertEquals(0.0, Metric.YELLOW_PER_GAME.extract(s), 0.0001);
        assertEquals(0.0, Metric.RED_PER_GAME.extract(s), 0.0001);
    }

    @Test
    void returnsZeroForMinutesShareWhenAppearancesAreZero() {
        PlayerStatsSnapshot s = new PlayerStatsSnapshot(
                1L, "Forward", 0, 900,
                10, 5, 4, 2, 3,
                8.5, 87.5, 2.1, 1.2, 1.3, 0.4, 0.5,
                1.1, 0.9, 1.4, 2.2, 0.3, 0.6, 2.5,
                50.0, 1.5, 3.5, 0.7, null
        );

        assertEquals(0.0, Metric.MINUTES_SHARE.extract(s), 0.0001);
    }

    @Test
    void returnsZeroForMotmWhenAppearancesAreZero() {
        PlayerStatsSnapshot s = new PlayerStatsSnapshot(
                1L, "Forward", 0, 900,
                10, 5, 4, 2, 3,
                8.5, 87.5, 2.1, 1.2, 1.3, 0.4, 0.5,
                1.1, 0.9, 1.4, 2.2, 0.3, 0.6, 2.5,
                50.0, 1.5, 3.5, 0.7, null
        );

        assertEquals(0.0, Metric.MOTM_PER_GAME.extract(s), 0.0001);
    }

    @Test
    void returnsZeroForGoalContributionsWhenMinutesAreZero() {
        PlayerStatsSnapshot s = new PlayerStatsSnapshot(
                1L, "Forward", 10, 0,
                10, 5, 4, 2, 3,
                8.5, 87.5, 2.1, 1.2, 1.3, 0.4, 0.5,
                1.1, 0.9, 1.4, 2.2, 0.3, 0.6, 2.5,
                50.0, 1.5, 3.5, 0.7, null
        );

        assertEquals(0.0, Metric.GOAL_CONTRIB_PER_90.extract(s), 0.0001);
    }
}