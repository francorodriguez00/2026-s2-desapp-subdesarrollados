package com.furbo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.furbo.model.PlayerSeasonStats;
import com.furbo.repository.PlayerRepository;
import com.furbo.repository.PlayerSeasonStatsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WhoScoredImportServiceTest {

    @Mock private PlayerSeasonStatsRepository repository;
    @Mock private PlayerMatcherService matcherService;
    private WhoScoredImportService importService;

    @TempDir Path tempDir;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        importService = new WhoScoredImportService(repository, matcherService);
    }

    @Test
    void testFullMappingAndTypeConversion(@TempDir Path tempDir) throws Exception {
        File leagueDir = tempDir.resolve("league").toFile();
        leagueDir.mkdirs();
        File jsonFile = new File(leagueDir, "page_1.json");
        
        Map<String, Object> p = new HashMap<>();
        p.put("playerId", 123L); p.put("teamId", 456); p.put("tournamentId", 789); p.put("seasonId", 101);
        p.put("name", "Test Player");
        p.put("rating", 8.5);
        p.put("goal", 5);
        p.put("yellowCard", 2);
        p.put("manOfTheMatch", true);
        
        Map<String, Object> data = new HashMap<>();
        data.put("playerTableStats", Collections.singletonList(p));
        new ObjectMapper().writeValue(jsonFile, data);

        importService.importLeague(leagueDir.getAbsolutePath());

        verify(repository).save(argThat(stats -> {
            return stats.getName().equals("Test Player") &&
                   stats.getRating() == 8.5 &&
                   stats.getGoals() == 5 &&
                   stats.getYellowCards() == 2 &&
                   stats.getManOfTheMatch() == true;
        }));
    }
}
