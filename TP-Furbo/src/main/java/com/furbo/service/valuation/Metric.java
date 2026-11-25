package com.furbo.service.valuation;

import java.util.function.Function;

public enum Metric {
    GOAL_CONTRIB_PER_90("goalContribPer90", s -> {
        double mins = s.minsPlayed() != null ? s.minsPlayed() : 0.0;
        if (mins == 0) return 0.0;
        return ((s.goals() != null ? s.goals() : 0) + (s.assists() != null ? s.assists() : 0)) / (mins / 90.0);
    }),
    KEY_PASSES("keyPasses", s -> s.keyPassPerGame() != null ? s.keyPassPerGame() : 0.0),
    THROUGH_BALLS("throughBalls", s -> s.accurateThroughBallPerGame() != null ? s.accurateThroughBallPerGame() : 0.0),
    LONG_PASSES("longPasses", s -> s.accurateLongPassPerGame() != null ? s.accurateLongPassPerGame() : 0.0),
    CROSSES("crosses", s -> s.accurateCrossesPerGame() != null ? s.accurateCrossesPerGame() : 0.0),
    TOTAL_PASSES("totalPasses", s -> s.totalPassesPerGame() != null ? s.totalPassesPerGame() : 0.0),
    PASS_SUCCESS("passSuccess", s -> s.passSuccess() != null ? s.passSuccess() : 0.0),
    DRIBBLES("dribbles", s -> s.dribbleWonPerGame() != null ? s.dribbleWonPerGame() : 0.0),
    TACKLES("tackles", s -> s.tacklePerGame() != null ? s.tacklePerGame() : 0.0),
    INTERCEPTIONS("interceptions", s -> s.interceptionPerGame() != null ? s.interceptionPerGame() : 0.0),
    CLEARANCES("clearances", s -> s.clearancePerGame() != null ? s.clearancePerGame() : 0.0),
    BLOCKS("blocks", s -> s.outfielderBlockPerGame() != null ? s.outfielderBlockPerGame() : 0.0),
    AERIAL_WON("aerialWon", s -> s.aerialWonPerGame() != null ? s.aerialWonPerGame() : 0.0),
    RATING("rating", s -> s.rating() != null ? s.rating() : 0.0),
    MINUTES_SHARE("minutesShare", s -> {
        if (s.apps() == null || s.apps() == 0) return 0.0;
        return (s.minsPlayed() != null ? s.minsPlayed() : 0.0) / (s.apps() * 90.0);
    }),
    MOTM_PER_GAME("motmPerGame", s -> {
        if (s.apps() == null || s.apps() == 0) return 0.0;
        return (s.manOfTheMatch() != null ? s.manOfTheMatch() : 0.0) / (double) s.apps();
    }),
    FOULS_DRAWN("foulsDrawn", s -> s.foulGivenPerGame() != null ? s.foulGivenPerGame() : 0.0),
    YELLOW_PER_GAME("yellowPerGame", s -> {
        if (s.apps() == null || s.apps() == 0) return 0.0;
        return (s.yellowCards() != null ? s.yellowCards() : 0.0) / (double) s.apps();
    }),
    RED_PER_GAME("redPerGame", s -> {
        if (s.apps() == null || s.apps() == 0) return 0.0;
        return (s.redCards() != null ? s.redCards() : 0.0) / (double) s.apps();
    }),
    FOULS("fouls", s -> s.foulsPerGame() != null ? s.foulsPerGame() : 0.0),
    TURNOVER("turnover", s -> s.turnoverPerGame() != null ? s.turnoverPerGame() : 0.0),
    DISPOSSESSED("dispossessed", s -> s.dispossessedPerGame() != null ? s.dispossessedPerGame() : 0.0),
    WAS_DRIBBLED("wasDribbled", s -> s.wasDribbledPerGame() != null ? s.wasDribbledPerGame() : 0.0);

    private final String code;
    private final Function<PlayerStatsSnapshot, Double> extractor;

    Metric(String code, Function<PlayerStatsSnapshot, Double> extractor) {
        this.code = code;
        this.extractor = extractor;
    }

    public String getCode() { return code; }
    public Double extract(PlayerStatsSnapshot s) { return extractor.apply(s); }
}
