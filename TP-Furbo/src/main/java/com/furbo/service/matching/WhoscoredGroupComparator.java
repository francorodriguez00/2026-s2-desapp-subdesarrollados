package com.furbo.service.matching;

import com.furbo.model.Player;
import com.furbo.model.PlayerSeasonStats;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class WhoscoredGroupComparator {

    private final MatchContext ctx;

    public WhoscoredGroupComparator(MatchContext ctx) {
        this.ctx = ctx;
    }

    public static int compareEntries(Map.Entry<String, List<Map<String, Object>>> e1, Map.Entry<String, List<Map<String, Object>>> e2, MatchContext ctx, Map<String, Long> existingInStatsMap) {
        Long wsId1 = Long.valueOf(e1.getKey());
        List<Map<String, Object>> rows1 = e1.getValue();
        Long wsId2 = Long.valueOf(e2.getKey());
        List<Map<String, Object>> rows2 = e2.getValue();

        Long existing1 = existingInStatsMap != null ? existingInStatsMap.get(e1.getKey()) : null;
        Long existing2 = existingInStatsMap != null ? existingInStatsMap.get(e2.getKey()) : null;

        boolean matchTeam1 = hasMatchingTeamMap(rows1, ctx, existing1);
        boolean matchTeam2 = hasMatchingTeamMap(rows2, ctx, existing2);

        if (matchTeam1 != matchTeam2) {
            return matchTeam1 ? -1 : 1;
        }

        int totalMins1 = rows1.stream().mapToInt(r -> r.get("minsPlayed") instanceof Number ? ((Number)r.get("minsPlayed")).intValue() : 0).sum();
        int totalMins2 = rows2.stream().mapToInt(r -> r.get("minsPlayed") instanceof Number ? ((Number)r.get("minsPlayed")).intValue() : 0).sum();

        if (totalMins1 != totalMins2) {
            return Integer.compare(totalMins2, totalMins1);
        }

        return wsId1.compareTo(wsId2);
    }

    public static int compareStatsEntries(Map.Entry<Long, List<PlayerSeasonStats>> e1, Map.Entry<Long, List<PlayerSeasonStats>> e2, MatchContext ctx) {
        Long wsId1 = e1.getKey();
        List<PlayerSeasonStats> rows1 = e1.getValue();
        Long wsId2 = e2.getKey();
        List<PlayerSeasonStats> rows2 = e2.getValue();

        boolean matchTeam1 = hasMatchingTeamStats(rows1, ctx);
        boolean matchTeam2 = hasMatchingTeamStats(rows2, ctx);

        if (matchTeam1 != matchTeam2) {
            return matchTeam1 ? -1 : 1;
        }

        int totalMins1 = rows1.stream().mapToInt(s -> s.getMinsPlayed() != null ? s.getMinsPlayed() : 0).sum();
        int totalMins2 = rows2.stream().mapToInt(s -> s.getMinsPlayed() != null ? s.getMinsPlayed() : 0).sum();

        if (totalMins1 != totalMins2) {
            return Integer.compare(totalMins2, totalMins1);
        }

        return wsId1.compareTo(wsId2);
    }

    private static Long getTargetPlayerIdFromMap(List<Map<String, Object>> rows, MatchContext ctx, Long existingInStats) {
        if (existingInStats != null) {
            return existingInStats;
        }

        if (ctx == null || ctx.getPlayersByName() == null || rows.isEmpty()) {
            return null;
        }

        Map<String, Object> bestRow = rows.stream()
                .max(Comparator.comparingInt(r -> r.get("minsPlayed") instanceof Number ? ((Number)r.get("minsPlayed")).intValue() : 0))
                .orElse(rows.get(0));

        String name = (String) bestRow.get("name");
        if (name == null) {
            return null;
        }

        String normalizedName = NameNormalizer.normalizeName(name);
        List<Player> candidates = ctx.getPlayersByName().get(normalizedName);
        if (candidates != null && candidates.size() == 1) {
            return candidates.get(0).getId();
        }

        return null;
    }

    private static Long getTargetPlayerIdFromStats(List<PlayerSeasonStats> rows, MatchContext ctx) {
        Long existingInStats = rows.stream()
                .map(PlayerSeasonStats::getPlayerId)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null);

        if (existingInStats != null) {
            return existingInStats;
        }

        if (ctx == null || ctx.getPlayersByName() == null || rows.isEmpty()) {
            return null;
        }

        PlayerSeasonStats bestRow = rows.stream()
                .max(Comparator.comparingInt(s -> s.getMinsPlayed() != null ? s.getMinsPlayed() : 0))
                .orElse(rows.get(0));

        String name = bestRow.getName();
        if (name == null) {
            return null;
        }

        String normalizedName = NameNormalizer.normalizeName(name);
        List<Player> candidates = ctx.getPlayersByName().get(normalizedName);
        if (candidates != null && candidates.size() == 1) {
            return candidates.get(0).getId();
        }

        return null;
    }

    private static boolean hasMatchingTeamMap(List<Map<String, Object>> rows, MatchContext ctx, Long existingInStats) {
        if (ctx == null || ctx.getPlayersById() == null) {
            return false;
        }
        Long targetPlayerId = getTargetPlayerIdFromMap(rows, ctx, existingInStats);
        if (targetPlayerId == null) {
            return false;
        }

        Player player = ctx.getPlayersById().get(targetPlayerId);
        if (player == null || player.getTeam() == null) {
            return false;
        }

        String normalizedPlayerTeam = NameNormalizer.normalizeTeam(player.getTeam());

        return rows.stream()
                .map(r -> (String) r.get("teamName"))
                .filter(java.util.Objects::nonNull)
                .map(NameNormalizer::normalizeTeam)
                .anyMatch(wsTeam -> normalizedPlayerTeam.contains(wsTeam) || wsTeam.contains(normalizedPlayerTeam));
    }

    private static boolean hasMatchingTeamStats(List<PlayerSeasonStats> rows, MatchContext ctx) {
        if (ctx == null || ctx.getPlayersById() == null) {
            return false;
        }
        Long targetPlayerId = getTargetPlayerIdFromStats(rows, ctx);
        if (targetPlayerId == null) {
            return false;
        }

        Player player = ctx.getPlayersById().get(targetPlayerId);
        if (player == null || player.getTeam() == null) {
            return false;
        }

        String normalizedPlayerTeam = NameNormalizer.normalizeTeam(player.getTeam());

        return rows.stream()
                .map(PlayerSeasonStats::getTeamName)
                .filter(java.util.Objects::nonNull)
                .map(NameNormalizer::normalizeTeam)
                .anyMatch(wsTeam -> normalizedPlayerTeam.contains(wsTeam) || wsTeam.contains(normalizedPlayerTeam));
    }
}
