package com.furbo.integration;

import com.furbo.config.TestcontainersConfig;
import com.furbo.model.*;
import com.furbo.repository.*;
import com.furbo.service.WhoScoredImportService;
import com.furbo.service.matching.PlayerLinkService;
import com.furbo.adapter.dto.RelinkSummaryDTO;
import com.furbo.service.matching.MatchContext;
import com.furbo.service.matching.WhoscoredGroupComparator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfig.class)
@Transactional
class PlayerLinkingIntegrationTest {

    @Autowired
    private WhoScoredImportService importService;

    @Autowired
    private PlayerLinkService linkService;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PlayerSeasonStatsRepository statsRepository;

    @Autowired
    private PlayerSourceIdRepository sourceRepository;

    private Path tempLeagueDir;

    @BeforeEach
    void setUp() throws Exception {
        statsRepository.deleteAll();
        sourceRepository.deleteAll();
        playerRepository.deleteAll();

        tempLeagueDir = Files.createTempDirectory("whoscored_test");
    }

    private void createMockPageJson(String filename, long playerId, String name, String teamName, int minsPlayed, double tacklePerGame) throws Exception {
        String json = """
            {
              "playerTableStats": [
                {
                  "playerId": %d,
                  "teamId": 10,
                  "tournamentId": 1,
                  "seasonId": 2024,
                  "name": "%s",
                  "firstName": "Test",
                  "lastName": "Player",
                  "age": 25,
                  "positionText": "M",
                  "playedPositions": "MC",
                  "teamName": "%s",
                  "tournamentName": "Premier League",
                  "seasonName": "2024/2025",
                  "height": 180,
                  "weight": 75,
                  "apps": 10,
                  "subOn": 2,
                  "minsPlayed": %d,
                  "rating": 7.0,
                  "goal": 1,
                  "assistTotal": 1,
                  "yellowCard": 1,
                  "redCard": 0,
                  "shotsPerGame": 1.0,
                  "aerialWonPerGame": 1.0,
                  "manOfTheMatch": 0,
                  "keyPassPerGame": 1.0,
                  "dribbleWonPerGame": 1.0,
                  "foulGivenPerGame": 1.0,
                  "dispossessedPerGame": 1.0,
                  "turnoverPerGame": 1.0,
                  "tacklePerGame": %.2f,
                  "interceptionPerGame": 1.0,
                  "foulsPerGame": 1.0,
                  "clearancePerGame": 1.0,
                  "wasDribbledPerGame": 1.0,
                  "outfielderBlockPerGame": 1.0,
                  "totalPassesPerGame": 30.0,
                  "accurateCrossesPerGame": 1.0,
                  "accurateLongPassPerGame": 1.0,
                  "accurateThroughBallPerGame": 1.0,
                  "passSuccess": 80.0
                }
              ]
            }
            """.formatted(playerId, name, teamName, minsPlayed, tacklePerGame);

        Path pageFile = tempLeagueDir.resolve(filename);
        Files.writeString(pageFile, json);
    }

    @Test
    void test1_importWithRealContextLinkingByName() throws Exception {
        Player player = new Player();
        player.setExternalId(999L);
        player.setName("Nico Gonzalez");
        player.setTeam("Juventus");
        player.setPosition("M");
        player.setNationality("Argentina");
        player.setLeague("Serie A");
        playerRepository.save(player);

        createMockPageJson("page_1.json", 422939L, "Nico Gonzalez", "Juventus", 900, 1.5);

        importService.importLeague(tempLeagueDir.toString());

        List<PlayerSeasonStats> stats = statsRepository.findAll();
        assertThat(stats).hasSize(1);
        assertThat(stats.get(0).getPlayerId()).isEqualTo(player.getId());
    }

    @Test
    void test2_importIdempotencyAndUpdate() throws Exception {
        Player player = new Player();
        player.setExternalId(999L);
        player.setName("Nico Gonzalez");
        player.setTeam("Juventus");
        player.setPosition("M");
        player.setNationality("Argentina");
        player.setLeague("Serie A");
        playerRepository.save(player);

        createMockPageJson("page_1.json", 422939L, "Nico Gonzalez", "Juventus", 900, 1.5);

        importService.importLeague(tempLeagueDir.toString());
        List<PlayerSeasonStats> firstRun = statsRepository.findAll();
        LocalDateTime firstUpdatedAt = firstRun.get(0).getUpdatedAt();
        LocalDateTime firstFetchedAt = firstRun.get(0).getFetchedAt();

        // Second run without changes
        importService.importLeague(tempLeagueDir.toString());
        List<PlayerSeasonStats> secondRun = statsRepository.findAll();
        assertThat(secondRun).hasSize(1);
        assertThat(secondRun.get(0).getUpdatedAt()).isEqualTo(firstUpdatedAt);

        // Third run with updated tacklePerGame
        Thread.sleep(10);
        createMockPageJson("page_1.json", 422939L, "Nico Gonzalez", "Juventus", 900, 2.5);
        importService.importLeague(tempLeagueDir.toString());
        List<PlayerSeasonStats> thirdRun = statsRepository.findAll();
        assertThat(thirdRun.get(0).getTacklePerGame()).isEqualTo(2.5);
        assertThat(thirdRun.get(0).getUpdatedAt()).isAfter(firstUpdatedAt);
        assertThat(thirdRun.get(0).getFetchedAt()).isNotNull();
    }

    @Test
    void test3_deterministicConflict() throws Exception {
        Player p1 = new Player();
        p1.setExternalId(101L);
        p1.setName("Vitinha");
        p1.setTeam("PSG");
        p1.setPosition("M");
        p1.setNationality("Portugal");
        p1.setLeague("Ligue 1");
        playerRepository.save(p1);

        // Both point to p1 initially via legacy or matcher
        // Let's create two WS ids that match p1 by name ("Vitinha")
        createMockPageJson("page_1.json", 384887L, "Vitinha", "PSG", 100, 1.0); 
        createMockPageJson("page_2.json", 407949L, "Vitinha", "Genoa", 2000, 1.0); 

        importService.importLeague(tempLeagueDir.toString());

        List<PlayerSeasonStats> stats = statsRepository.findAll();
        long linkedCount = stats.stream().filter(s -> s.getPlayerId() != null).count();
        long nullCount = stats.stream().filter(s -> s.getPlayerId() == null).count();

        assertThat(linkedCount).isEqualTo(1);
        assertThat(nullCount).isEqualTo(1);
    }

    @Test
    void test4_relinkAllIdempotency() throws Exception {
        Player p1 = new Player();
        p1.setExternalId(101L);
        p1.setName("Vitinha");
        p1.setTeam("PSG");
        playerRepository.save(p1);

        createMockPageJson("page_1.json", 384887L, "Vitinha", "PSG", 500, 1.0);

        importService.importLeague(tempLeagueDir.toString());

        RelinkSummaryDTO summary1 = linkService.relinkAll();
        RelinkSummaryDTO summary2 = linkService.relinkAll();

        assertThat(summary2.alreadyLinked()).isGreaterThanOrEqualTo(summary1.alreadyLinked());
        assertThat(summary2.legacyCreated()).isEqualTo(0);
    }

    @Test
    void test5_reviewAndUnmatchedDoNotOverwriteExistingPlayerId() throws Exception {
        Player p1 = new Player();
        p1.setExternalId(101L);
        p1.setName("Homonym Player");
        p1.setTeam("Team A");
        playerRepository.save(p1);

        Player p2 = new Player();
        p2.setExternalId(102L);
        p2.setName("Homonym Player");
        p2.setTeam("Team B");
        playerRepository.save(p2);

        createMockPageJson("page_1.json", 999999L, "Homonym Player", "Team X", 500, 1.0);

        importService.importLeague(tempLeagueDir.toString());
        List<PlayerSeasonStats> stats = statsRepository.findAll();
        assertThat(stats.get(0).getPlayerId()).isNull(); // Review or unmatched
    }

    @Test
    void test6_comparatorUnit() {
        MatchContext ctx = new MatchContext(new java.util.HashMap<>(), new java.util.HashMap<>(), new java.util.HashMap<>(), new java.util.HashMap<>(), new java.util.HashMap<>(), new java.util.HashSet<>());
        java.util.Map.Entry<String, List<java.util.Map<String, Object>>> e1 = new java.util.AbstractMap.SimpleEntry<>("1", List.of(java.util.Map.of("minsPlayed", 100)));
        java.util.Map.Entry<String, List<java.util.Map<String, Object>>> e2 = new java.util.AbstractMap.SimpleEntry<>("2", List.of(java.util.Map.of("minsPlayed", 200)));
        int res = WhoscoredGroupComparator.compareEntries(e1, e2, ctx, null);
        assertThat(res).isNotZero();
    }


    @Test
    void test7_relinkSummaryLists() throws Exception {
        createMockPageJson("page_1.json", 555555L, "Unmatched Guy", "Unknown", 300, 1.0);
        importService.importLeague(tempLeagueDir.toString());

        RelinkSummaryDTO summary = linkService.relinkAll();
        assertThat(summary.unmatchedPlayers()).isNotEmpty();
    }
}
