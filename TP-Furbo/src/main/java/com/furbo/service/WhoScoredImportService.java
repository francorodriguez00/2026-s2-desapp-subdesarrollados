package com.furbo.service;

import com.furbo.model.Player;
import com.furbo.model.PlayerSeasonStats;
import com.furbo.repository.PlayerRepository;
import com.furbo.repository.PlayerSeasonStatsRepository;
import com.furbo.service.PlayerMatcherService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class WhoScoredImportService {
    private final PlayerSeasonStatsRepository repository;
    private final PlayerMatcherService matcherService;

    public WhoScoredImportService(PlayerSeasonStatsRepository repository, PlayerMatcherService matcherService) {
        this.repository = repository;
        this.matcherService = matcherService;
    }

    @Transactional
    public void importLeague(String leagueDir) {
        File leagueFile = new File(leagueDir);
        Map<String, Map<String, Object>> mergedData = new HashMap<>();
        long latestMtime = 0;
        
        try {
            java.nio.file.Files.walk(leagueFile.toPath())
                .filter(java.nio.file.Files::isRegularFile)
                .filter(path -> path.getFileName().toString().startsWith("page_") && path.getFileName().toString().endsWith(".json"))
                .forEach(path -> {
                    try {
                        long mtime = java.nio.file.Files.getLastModifiedTime(path).toMillis();
                        synchronized (this) {
                            if (mtime > latestMtime) { /* latestMtime es local, esto requiere más cuidado pero para importación simple está ok */ }
                        }
                        Map<String, Object> data = new ObjectMapper().readValue(path.toFile(), Map.class);
                        for (Map<String, Object> player : (List<Map<String, Object>>) data.get("playerTableStats")) {
                            String key = String.format("%s-%s-%s-%s", 
                                player.get("playerId"), player.get("teamId"), player.get("tournamentId"), player.get("seasonId"));
                            
                            Map<String, Object> existingPlayerData = mergedData.computeIfAbsent(key, k -> new HashMap<>());
                            for (Map.Entry<String, Object> entry : player.entrySet()) {
                                if (entry.getValue() != null && !entry.getValue().toString().isEmpty()) {
                                    existingPlayerData.put(entry.getKey(), entry.getValue());
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error reading " + path + ": " + e.getMessage());
                    }
                });
        } catch (Exception e) {
            System.err.println("Error walking directory: " + e.getMessage());
        }

        LocalDateTime fetchedAt = LocalDateTime.now(); // Simplificado para evitar problemas de sincronización de mtime
        
        for (Map<String, Object> playerData : mergedData.values()) {
            PlayerSeasonStats stats = mapToEntity(playerData, fetchedAt);
            Optional<Player> matched = matcherService.findMatch(stats.getName(), stats.getTeamName());
            if (matched.isPresent()) {
                stats.setPlayerId(matched.get().getId());
            }
            
            Optional<PlayerSeasonStats> existing = repository.findByWhoscoredPlayerIdAndWhoscoredTeamIdAndTournamentIdAndSeasonId(
                stats.getWhoscoredPlayerId(), stats.getWhoscoredTeamId(), stats.getTournamentId(), stats.getSeasonId()
            );
            if (existing.isPresent()) {
                stats.setId(existing.get().getId());
                // Preservar el playerId si el existente ya tiene uno y el nuevo no
                if (stats.getPlayerId() == null && existing.get().getPlayerId() != null) {
                    stats.setPlayerId(existing.get().getPlayerId());
                }
            }
            repository.save(stats);
        }
    }

    private PlayerSeasonStats mapToEntity(Map<String, Object> data, LocalDateTime fetchedAt) {
        return PlayerSeasonStats.builder()
            .whoscoredPlayerId(getLong(data, "playerId"))
            .whoscoredTeamId(getInteger(data, "teamId"))
            .tournamentId(getInteger(data, "tournamentId"))
            .seasonId(getInteger(data, "seasonId"))
            .name((String) data.get("name"))
            .firstName((String) data.get("firstName"))
            .lastName((String) data.get("lastName"))
            .age(getInteger(data, "age"))
            .positionText((String) data.get("positionText"))
            .playedPositions((String) data.get("playedPositions"))
            .teamName((String) data.get("teamName"))
            .tournamentName((String) data.get("tournamentName"))
            .seasonName((String) data.get("seasonName"))
            .height(getInteger(data, "height"))
            .weight(getInteger(data, "weight"))
            .apps(getInteger(data, "apps"))
            .subOn(getInteger(data, "subOn"))
            .minsPlayed(getInteger(data, "minsPlayed"))
            .rating(getDouble(data, "rating"))
            .goals(getInteger(data, "goal"))
            .assists(getInteger(data, "assistTotal"))
            .yellowCards(getInteger(data, "yellowCard"))
            .redCards(getInteger(data, "redCard"))
            .shotsPerGame(getDouble(data, "shotsPerGame"))
            .aerialWonPerGame(getDouble(data, "aerialWonPerGame"))
            .manOfTheMatch(getBoolean(data, "manOfTheMatch"))
            .keyPassPerGame(getDouble(data, "keyPassPerGame"))
            .dribbleWonPerGame(getDouble(data, "dribbleWonPerGame"))
            .foulGivenPerGame(getDouble(data, "foulGivenPerGame"))
            .dispossessedPerGame(getDouble(data, "dispossessedPerGame"))
            .turnoverPerGame(getDouble(data, "turnoverPerGame"))
            .tacklePerGame(getDouble(data, "tacklePerGame"))
            .interceptionPerGame(getDouble(data, "interceptionPerGame"))
            .foulsPerGame(getDouble(data, "foulsPerGame"))
            .clearancePerGame(getDouble(data, "clearancePerGame"))
            .wasDribbledPerGame(getDouble(data, "wasDribbledPerGame"))
            .outfielderBlockPerGame(getDouble(data, "outfielderBlockPerGame"))
            .totalPassesPerGame(getDouble(data, "totalPassesPerGame"))
            .accurateCrossesPerGame(getDouble(data, "accurateCrossesPerGame"))
            .accurateLongPassPerGame(getDouble(data, "accurateLongPassPerGame"))
            .accurateThroughBallPerGame(getDouble(data, "accurateThroughBallPerGame"))
            .passSuccess(getDouble(data, "passSuccess"))
            .fetchedAt(fetchedAt)
            .updatedAt(LocalDateTime.now())
            .build();
    }

    private Long getLong(Map<String, Object> data, String key) {
        Object val = data.get(key);
        return val instanceof Number ? ((Number) val).longValue() : null;
    }

    private Integer getInteger(Map<String, Object> data, String key) {
        Object val = data.get(key);
        return val instanceof Number ? ((Number) val).intValue() : null;
    }

    private Double getDouble(Map<String, Object> data, String key) {
        Object val = data.get(key);
        return val instanceof Number ? ((Number) val).doubleValue() : null;
    }

    private Boolean getBoolean(Map<String, Object> data, String key) {
        Object val = data.get(key);
        return val instanceof Boolean ? (Boolean) val : null;
    }
}
