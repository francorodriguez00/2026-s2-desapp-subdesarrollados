package com.furbo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.furbo.adapter.dto.UnmatchedPlayerDTO;
import com.furbo.model.*;
import com.furbo.repository.*;
import com.furbo.service.matching.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WhoScoredImportService {
    private static final Logger log = LoggerFactory.getLogger(WhoScoredImportService.class);

    private final PlayerSeasonStatsRepository repository;
    private final PlayerMatcherService matcherService;
    private final PlayerLinkResolver linkResolver;

    public WhoScoredImportService(PlayerSeasonStatsRepository repository, 
                                  PlayerMatcherService matcherService,
                                  PlayerLinkResolver linkResolver) {
        this.repository = repository;
        this.matcherService = matcherService;
        this.linkResolver = linkResolver;
    }

    @Transactional
    public void importLeague(String leagueDir) {
        log.info("Iniciando importación en {}", leagueDir);
        Map<String, Map<String, Object>> mergedData = readAndMerge(leagueDir);
        if (mergedData.isEmpty()) {
            log.warn("No se encontraron archivos JSON en la ruta absoluta: {}", new File(leagueDir).getAbsolutePath());
            return;
        }

        List<PlayerSeasonStats> existingStats = repository.findAll();
        Map<String, PlayerSeasonStats> statsMap = new HashMap<>();
        for (PlayerSeasonStats s : existingStats) {
            String key = String.format("%d-%d-%d-%d", 
                s.getWhoscoredPlayerId(), s.getWhoscoredTeamId(), s.getTournamentId(), s.getSeasonId());
            statsMap.put(key, s);
        }

        LocalDateTime fetchedAt = LocalDateTime.now();
        Map<String, List<Map<String, Object>>> groupedByWsId = new HashMap<>();
        for (Map.Entry<String, Map<String, Object>> entry : mergedData.entrySet()) {
            String wsId = String.valueOf(entry.getValue().get("playerId"));
            groupedByWsId.computeIfAbsent(wsId, k -> new ArrayList<>()).add(entry.getValue());
        }

        MatchContext ctx = matcherService.buildContext();
        int newFiles = 0, updated = 0, unchanged = 0;
        Map<LinkOutcome.Type, Integer> outcomes = new EnumMap<>(LinkOutcome.Type.class);
        List<PlayerSeasonStats> toSave = new ArrayList<>();
        List<ConflictInfo> conflictsList = new ArrayList<>();

        Map<String, Long> existingInStatsMap = new HashMap<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : groupedByWsId.entrySet()) {
            Long existing = entry.getValue().stream()
                .map(r -> statsMap.get(buildKey(r)))
                .filter(Objects::nonNull)
                .map(PlayerSeasonStats::getPlayerId)
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
            if (existing != null) {
                existingInStatsMap.put(entry.getKey(), existing);
            }
        }

        List<Map.Entry<String, List<Map<String, Object>>>> pass1Entries = new ArrayList<>();
        List<Map.Entry<String, List<Map<String, Object>>>> pass2Entries = new ArrayList<>();

        for (Map.Entry<String, List<Map<String, Object>>> entry : groupedByWsId.entrySet()) {
            String wsId = entry.getKey();
            List<Map<String, Object>> rows = entry.getValue();
            WhoScoredPlayerRef ref = buildRef(wsId, rows);
            Long existingInStats = existingInStatsMap.get(wsId);

            LinkOutcome outcome = linkResolver.resolve(wsId, ref, existingInStats, ctx, false);
            if (outcome.getOutcome() == LinkOutcome.Type.UNMATCHED) {
                pass2Entries.add(entry);
            } else {
                pass1Entries.add(entry);
            }
        }

        pass1Entries.sort((e1, e2) -> WhoscoredGroupComparator.compareEntries(e1, e2, ctx, existingInStatsMap));
        pass2Entries.sort((e1, e2) -> {
            int mins1 = e1.getValue().stream().mapToInt(r -> r.get("minsPlayed") instanceof Number ? ((Number)r.get("minsPlayed")).intValue() : 0).sum();
            int mins2 = e2.getValue().stream().mapToInt(r -> r.get("minsPlayed") instanceof Number ? ((Number)r.get("minsPlayed")).intValue() : 0).sum();
            if (mins1 != mins2) {
                return Integer.compare(mins2, mins1);
            }
            return Long.compare(Long.parseLong(e1.getKey()), Long.parseLong(e2.getKey()));
        });

        List<Map.Entry<String, List<Map<String, Object>>>> sortedEntries = new ArrayList<>();
        sortedEntries.addAll(pass1Entries);
        sortedEntries.addAll(pass2Entries);

        for (Map.Entry<String, List<Map<String, Object>>> entry : sortedEntries) {
            String wsId = entry.getKey();
            List<Map<String, Object>> rows = entry.getValue();

            WhoScoredPlayerRef ref = buildRef(wsId, rows);
            Long existingInStats = existingInStatsMap.get(wsId);

            LinkOutcome outcome = linkResolver.resolve(wsId, ref, existingInStats, ctx, true);
            outcomes.put(outcome.getOutcome(), outcomes.getOrDefault(outcome.getOutcome(), 0) + 1);
            
            if (outcome.getOutcome() == LinkOutcome.Type.CONFLICT) {
                String ownerExternalId = "";
                var existingSource = linkResolver.tryFindExistingByPlayerId(outcome.getDisputedPlayerId());
                if (existingSource.isPresent()) {
                    ownerExternalId = existingSource.get().getExternalId();
                }
                conflictsList.add(new ConflictInfo(wsId, ref.name(), ref.teamNames().get(0), rows.stream().mapToInt(r -> getInteger(r, "minsPlayed")).sum(), outcome.getDisputedPlayerId(), ownerExternalId));
                repository.clearPlayerIdForWhoscoredId(Long.valueOf(wsId));
            } else if (outcome.getPlayerId() != null) {
                repository.setPlayerIdForWhoscoredId(Long.valueOf(wsId), outcome.getPlayerId());
            }

            for (Map<String, Object> row : rows) {
                String key = buildKey(row);
                PlayerSeasonStats stats = statsMap.getOrDefault(key, mapToEntity(row, fetchedAt));
                
                boolean changed = false;
                if (stats.getId() != null) {
                    changed = updateFields(stats, row);
                    if (changed) {
                        stats.setUpdatedAt(LocalDateTime.now());
                        updated++;
                    } else {
                        unchanged++;
                    }
                } else {
                    newFiles++;
                }

                if (outcome.getOutcome() == LinkOutcome.Type.CONFLICT) {
                    stats.setPlayerId(null);
                } else if (outcome.getPlayerId() != null) {
                    stats.setPlayerId(outcome.getPlayerId());
                }
                
                stats.setFetchedAt(fetchedAt);
                toSave.add(stats);
            }
        }
        repository.saveAll(toSave);
        
        log.info("Importación finalizada. Nuevas: {}, Actualizadas: {}, Sin cambios: {}. Outcomes: {}", 
            newFiles, updated, unchanged, outcomes);
        
        List<UnmatchedPlayerDTO> unmatched = toSave.stream()
            .filter(s -> s.getPlayerId() == null)
            .collect(Collectors.groupingBy(PlayerSeasonStats::getWhoscoredPlayerId))
            .entrySet().stream()
            .map(e -> {
                PlayerSeasonStats best = e.getValue().stream().max(Comparator.comparingInt(s -> s.getMinsPlayed())).orElse(e.getValue().get(0));
                return new UnmatchedPlayerDTO(String.valueOf(e.getKey()), best.getName(), best.getTeamName(), 
                    best.getTournamentName(), best.getPositionText(), 
                    e.getValue().stream().mapToInt(PlayerSeasonStats::getMinsPlayed).sum());
            })
            .sorted(Comparator.comparingInt(UnmatchedPlayerDTO::totalMinutes).reversed())
            .limit(30)
            .toList();
        
        log.info("Jugadores sin vincular totales: {}", unmatched.size());
        for (UnmatchedPlayerDTO u : unmatched) {
            log.info("Sin vincular -> {}", u);
        }
        log.info("Conflictos totales: {}", conflictsList.size());
        for (ConflictInfo c : conflictsList) {
            log.info("Conflicto -> {}", c);
        }
    }

    private String buildKey(Map<String, Object> row) {
        return String.format("%s-%s-%s-%s", row.get("playerId"), row.get("teamId"), 
            row.get("tournamentId"), row.get("seasonId"));
    }

    private boolean updateFields(PlayerSeasonStats s, Map<String, Object> data) {
        boolean changed = false;

        // Ratings & Stats
        double newRating = getDouble(data, "rating");
        if (!Objects.equals(s.getRating(), newRating)) { s.setRating(newRating); changed = true; }

        int newGoals = getInteger(data, "goal");
        if (!Objects.equals(s.getGoals(), newGoals)) { s.setGoals(newGoals); changed = true; }

        int newAssists = getInteger(data, "assistTotal");
        if (!Objects.equals(s.getAssists(), newAssists)) { s.setAssists(newAssists); changed = true; }

        int newYellowCards = getInteger(data, "yellowCard");
        if (!Objects.equals(s.getYellowCards(), newYellowCards)) { s.setYellowCards(newYellowCards); changed = true; }

        int newRedCards = getInteger(data, "redCard");
        if (!Objects.equals(s.getRedCards(), newRedCards)) { s.setRedCards(newRedCards); changed = true; }

        double newShotsPerGame = getDouble(data, "shotsPerGame");
        if (!Objects.equals(s.getShotsPerGame(), newShotsPerGame)) { s.setShotsPerGame(newShotsPerGame); changed = true; }

        double newAerialWonPerGame = getDouble(data, "aerialWonPerGame");
        if (!Objects.equals(s.getAerialWonPerGame(), newAerialWonPerGame)) { s.setAerialWonPerGame(newAerialWonPerGame); changed = true; }

        int newManOfTheMatch = getInteger(data, "manOfTheMatch");
        if (!Objects.equals(s.getManOfTheMatch(), newManOfTheMatch)) { s.setManOfTheMatch(newManOfTheMatch); changed = true; }

        double newKeyPassPerGame = getDouble(data, "keyPassPerGame");
        if (!Objects.equals(s.getKeyPassPerGame(), newKeyPassPerGame)) { s.setKeyPassPerGame(newKeyPassPerGame); changed = true; }

        double newDribbleWonPerGame = getDouble(data, "dribbleWonPerGame");
        if (!Objects.equals(s.getDribbleWonPerGame(), newDribbleWonPerGame)) { s.setDribbleWonPerGame(newDribbleWonPerGame); changed = true; }

        double newFoulGivenPerGame = getDouble(data, "foulGivenPerGame");
        if (!Objects.equals(s.getFoulGivenPerGame(), newFoulGivenPerGame)) { s.setFoulGivenPerGame(newFoulGivenPerGame); changed = true; }

        double newDispossessedPerGame = getDouble(data, "dispossessedPerGame");
        if (!Objects.equals(s.getDispossessedPerGame(), newDispossessedPerGame)) { s.setDispossessedPerGame(newDispossessedPerGame); changed = true; }

        double newTurnoverPerGame = getDouble(data, "turnoverPerGame");
        if (!Objects.equals(s.getTurnoverPerGame(), newTurnoverPerGame)) { s.setTurnoverPerGame(newTurnoverPerGame); changed = true; }

        double newTacklePerGame = getDouble(data, "tacklePerGame");
        if (!Objects.equals(s.getTacklePerGame(), newTacklePerGame)) { s.setTacklePerGame(newTacklePerGame); changed = true; }

        double newInterceptionPerGame = getDouble(data, "interceptionPerGame");
        if (!Objects.equals(s.getInterceptionPerGame(), newInterceptionPerGame)) { s.setInterceptionPerGame(newInterceptionPerGame); changed = true; }

        double newFoulsPerGame = getDouble(data, "foulsPerGame");
        if (!Objects.equals(s.getFoulsPerGame(), newFoulsPerGame)) { s.setFoulsPerGame(newFoulsPerGame); changed = true; }

        double newClearancePerGame = getDouble(data, "clearancePerGame");
        if (!Objects.equals(s.getClearancePerGame(), newClearancePerGame)) { s.setClearancePerGame(newClearancePerGame); changed = true; }

        double newWasDribbledPerGame = getDouble(data, "wasDribbledPerGame");
        if (!Objects.equals(s.getWasDribbledPerGame(), newWasDribbledPerGame)) { s.setWasDribbledPerGame(newWasDribbledPerGame); changed = true; }

        double newOutfielderBlockPerGame = getDouble(data, "outfielderBlockPerGame");
        if (!Objects.equals(s.getOutfielderBlockPerGame(), newOutfielderBlockPerGame)) { s.setOutfielderBlockPerGame(newOutfielderBlockPerGame); changed = true; }

        double newTotalPassesPerGame = getDouble(data, "totalPassesPerGame");
        if (!Objects.equals(s.getTotalPassesPerGame(), newTotalPassesPerGame)) { s.setTotalPassesPerGame(newTotalPassesPerGame); changed = true; }

        double newAccurateCrossesPerGame = getDouble(data, "accurateCrossesPerGame");
        if (!Objects.equals(s.getAccurateCrossesPerGame(), newAccurateCrossesPerGame)) { s.setAccurateCrossesPerGame(newAccurateCrossesPerGame); changed = true; }

        double newAccurateLongPassPerGame = getDouble(data, "accurateLongPassPerGame");
        if (!Objects.equals(s.getAccurateLongPassPerGame(), newAccurateLongPassPerGame)) { s.setAccurateLongPassPerGame(newAccurateLongPassPerGame); changed = true; }

        double newAccurateThroughBallPerGame = getDouble(data, "accurateThroughBallPerGame");
        if (!Objects.equals(s.getAccurateThroughBallPerGame(), newAccurateThroughBallPerGame)) { s.setAccurateThroughBallPerGame(newAccurateThroughBallPerGame); changed = true; }

        double newPassSuccess = getDouble(data, "passSuccess");
        if (!Objects.equals(s.getPassSuccess(), newPassSuccess)) { s.setPassSuccess(newPassSuccess); changed = true; }

        // General Info
        String newFirstName = (String) data.get("firstName");
        if (!Objects.equals(s.getFirstName(), newFirstName)) { s.setFirstName(newFirstName); changed = true; }

        String newLastName = (String) data.get("lastName");
        if (!Objects.equals(s.getLastName(), newLastName)) { s.setLastName(newLastName); changed = true; }

        String newPlayedPositions = (String) data.get("playedPositions");
        if (!Objects.equals(s.getPlayedPositions(), newPlayedPositions)) { s.setPlayedPositions(newPlayedPositions); changed = true; }

        int newAge = getInteger(data, "age");
        if (!Objects.equals(s.getAge(), newAge)) { s.setAge(newAge); changed = true; }

        String newPositionText = (String) data.get("positionText");
        if (!Objects.equals(s.getPositionText(), newPositionText)) { s.setPositionText(newPositionText); changed = true; }

        String newTeamName = (String) data.get("teamName");
        if (!Objects.equals(s.getTeamName(), newTeamName)) { s.setTeamName(newTeamName); changed = true; }

        String newTournamentName = (String) data.get("tournamentName");
        if (!Objects.equals(s.getTournamentName(), newTournamentName)) { s.setTournamentName(newTournamentName); changed = true; }

        String newSeasonName = (String) data.get("seasonName");
        if (!Objects.equals(s.getSeasonName(), newSeasonName)) { s.setSeasonName(newSeasonName); changed = true; }

        int newHeight = getInteger(data, "height");
        if (!Objects.equals(s.getHeight(), newHeight)) { s.setHeight(newHeight); changed = true; }

        int newWeight = getInteger(data, "weight");
        if (!Objects.equals(s.getWeight(), newWeight)) { s.setWeight(newWeight); changed = true; }

        int newApps = getInteger(data, "apps");
        if (!Objects.equals(s.getApps(), newApps)) { s.setApps(newApps); changed = true; }

        int newSubOn = getInteger(data, "subOn");
        if (!Objects.equals(s.getSubOn(), newSubOn)) { s.setSubOn(newSubOn); changed = true; }

        int newMinsPlayed = getInteger(data, "minsPlayed");
        if (!Objects.equals(s.getMinsPlayed(), newMinsPlayed)) { s.setMinsPlayed(newMinsPlayed); changed = true; }

        return changed;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Map<String, Object>> readAndMerge(String dir) {
        Map<String, Map<String, Object>> mergedData = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        int files = 0;
        try (java.util.stream.Stream<java.nio.file.Path> walk = java.nio.file.Files.walk(new File(dir).toPath())) {
            List<java.nio.file.Path> pages = walk
                .filter(java.nio.file.Files::isRegularFile)
                .filter(p -> p.getFileName().toString().startsWith("page_") && p.getFileName().toString().endsWith(".json"))
                .toList();
            for (java.nio.file.Path path : pages) {
                files++;
                try {
                    Map<String, Object> data = mapper.readValue(path.toFile(), Map.class);
                    List<Map<String, Object>> players = (List<Map<String, Object>>) data.get("playerTableStats");
                    if (players == null) continue;
                    for (Map<String, Object> player : players) {
                        String key = String.format("%s-%s-%s-%s", 
                            player.get("playerId"), player.get("teamId"), player.get("tournamentId"), player.get("seasonId"));
                        Map<String, Object> merged = mergedData.computeIfAbsent(key, k -> new HashMap<>());
                        for (Map.Entry<String, Object> e : player.entrySet()) {
                            if (e.getValue() != null && !e.getValue().toString().isEmpty()) merged.put(e.getKey(), e.getValue());
                        }
                    }
                } catch (Exception e) { log.error("Error leyendo {}", path, e); }
            }
        } catch (Exception e) { log.error("Error recorriendo {}", dir, e); }
        return mergedData;
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
            .manOfTheMatch(getInteger(data, "manOfTheMatch"))
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
        return val instanceof Number ? ((Number) val).intValue() : 0;
    }

    private Double getDouble(Map<String, Object> data, String key) {
        Object val = data.get(key);
        return val instanceof Number ? ((Number) val).doubleValue() : 0.0;
    }

    private record ConflictInfo(String wsId, String name, String team, int minutes, Long disputedPlayerId, String ownerExternalId) {}

    private WhoScoredPlayerRef buildRef(String wsId, List<Map<String, Object>> rows) {
        Map<String, Object> bestRow = rows.stream()
            .max(Comparator.comparingInt(r -> getInteger(r, "minsPlayed")))
            .orElse(rows.get(0));
        Set<String> teams = new HashSet<>();
        Set<Integer> tournaments = new HashSet<>();
        for (Map<String, Object> r : rows) {
            teams.add((String) r.get("teamName"));
            tournaments.add(getInteger(r, "tournamentId"));
        }
        return new WhoScoredPlayerRef(wsId, (String)bestRow.get("name"), 
            (String)bestRow.get("firstName"), (String)bestRow.get("lastName"), 
            new ArrayList<>(teams), new ArrayList<>(tournaments), (String)bestRow.get("positionText"));
    }

    // Clase interna para el reporte de jugadores sin vincular
    private record UnmatchedPlayerDTO(String wsId, String name, String team, 
        String tournamentName, String positionText, int totalMinutes) {}
}