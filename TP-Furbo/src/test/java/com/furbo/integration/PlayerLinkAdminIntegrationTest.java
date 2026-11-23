package com.furbo.integration;

import com.furbo.config.TestcontainersConfig;
import com.furbo.model.*;
import com.furbo.repository.*;
import com.furbo.service.WhoScoredImportService;
import com.furbo.service.matching.PlayerLinkAdminService;
import com.furbo.service.matching.PlayerLinkService;
import com.furbo.service.matching.PlayerMatchingPersister;
import com.furbo.service.matching.MatchOutcome;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class PlayerLinkAdminIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WhoScoredImportService importService;

    @Autowired
    private PlayerLinkAdminService adminService;

    @Autowired
    private PlayerLinkService linkService;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PlayerSeasonStatsRepository statsRepository;

    @Autowired
    private PlayerSourceIdRepository sourceRepository;

    @Autowired
    private PlayerMatchReviewRepository reviewRepository;

    @Autowired
    private PlayerMatchingPersister persister;

    private Path tempLeagueDir;

    @BeforeEach
    void setUp() throws Exception {
        reviewRepository.deleteAll();
        statsRepository.deleteAll();
        sourceRepository.deleteAll();
        playerRepository.deleteAll();

        tempLeagueDir = Files.createTempDirectory("admin_test");
    }

    private void createMockPageJson(String filename, long playerId, String name, String teamName, int minsPlayed) throws Exception {
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
                  "tacklePerGame": 1.0,
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
            """.formatted(playerId, name, teamName, minsPlayed);

        Path pageFile = tempLeagueDir.resolve(filename);
        Files.writeString(pageFile, json);
    }

    @Test
    void test1_linkManuallyHappyPath() throws Exception {
        Player player = new Player();
        player.setExternalId(123L);
        player.setName("Kevin De Bruyne");
        player.setTeam("Manchester City");
        playerRepository.save(player);

        createMockPageJson("page_1.json", 777L, "K. De Bruyne", "Man City", 500);
        importService.importLeague(tempLeagueDir.toString());

        String body = objectMapper.writeValueAsString(new com.furbo.adapter.dto.LinkManualRequestDTO(player.getId()));

        mockMvc.perform(post("/admin/player-links/links/777")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action").value("LINKED"))
                .andExpect(jsonPath("$.playerId").value(player.getId()));

        Optional<PlayerSourceId> link = sourceRepository.findBySourceAndExternalId("WHOSCORED", "777");
        assertThat(link).isPresent();
        assertThat(link.get().getMatchMethod()).isEqualTo("MANUAL");
        assertThat(link.get().getConfidence()).isEqualByComparingTo("1.000");

        List<PlayerSeasonStats> stats = statsRepository.findByWhoscoredPlayerId(777L);
        assertThat(stats).isNotEmpty();
        for (PlayerSeasonStats s : stats) {
            assertThat(s.getPlayerId()).isEqualTo(player.getId());
        }
    }

    @Test
    void test2_linkManuallyValidationAndConflicts() throws Exception {
        Player p1 = new Player();
        p1.setExternalId(1L);
        p1.setName("Player One");
        playerRepository.save(p1);

        Player p2 = new Player();
        p2.setExternalId(2L);
        p2.setName("Player Two");
        playerRepository.save(p2);

        createMockPageJson("page_1.json", 100L, "Stats 100", "Team A", 100);
        createMockPageJson("page_2.json", 200L, "Stats 200", "Team B", 100);
        importService.importLeague(tempLeagueDir.toString());

        // Non-numeric externalId -> 400
        mockMvc.perform(post("/admin/player-links/links/abc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.furbo.adapter.dto.LinkManualRequestDTO(p1.getId()))))
                .andExpect(status().isBadRequest());

        // Non-existent stats -> 404
        mockMvc.perform(post("/admin/player-links/links/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.furbo.adapter.dto.LinkManualRequestDTO(p1.getId()))))
                .andExpect(status().isNotFound());

        // Non-existent player -> 404
        mockMvc.perform(post("/admin/player-links/links/100")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.furbo.adapter.dto.LinkManualRequestDTO(9999L))))
                .andExpect(status().isNotFound());

        // Link 100 to p1
        adminService.linkManually("100", p1.getId());

        // Idempotent same link -> ALREADY_LINKED_SAME
        mockMvc.perform(post("/admin/player-links/links/100")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.furbo.adapter.dto.LinkManualRequestDTO(p1.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action").value("ALREADY_LINKED_SAME"));

        // Try linking 100 to p2 -> 409 Conflict
        mockMvc.perform(post("/admin/player-links/links/100")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.furbo.adapter.dto.LinkManualRequestDTO(p2.getId()))))
                .andExpect(status().isConflict());

        // Try linking 200 to p1 (p1 already linked to 100) -> 409 Conflict
        mockMvc.perform(post("/admin/player-links/links/200")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.furbo.adapter.dto.LinkManualRequestDTO(p1.getId()))))
                .andExpect(status().isConflict());
    }

    @Test
    void test3_approveMatchCandidate() throws Exception {
        Player player = new Player();
        player.setExternalId(10L);
        player.setName("Exact Name");
        playerRepository.save(player);

        PlayerSeasonStats stat = PlayerSeasonStats.builder()
            .whoscoredPlayerId(300L)
            .whoscoredTeamId(10)
            .tournamentId(1)
            .seasonId(2024)
            .name("Exact Name")
            .teamName("Other Team")
            .minsPlayed(500)
            .build();
        statsRepository.save(stat);

        PlayerMatchReview rev = PlayerMatchReview.builder()
            .source("WHOSCORED")
            .externalId("300")
            .candidatePlayerId(player.getId())
            .score(0.85)
            .reason("TEST")
            .status(ReviewStatus.PENDING)
            .createdAt(java.time.LocalDateTime.now())
            .build();
        reviewRepository.save(rev);

        // Approve with invalid candidate -> 400
        mockMvc.perform(post("/admin/player-links/reviews/300/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.furbo.adapter.dto.ApproveRequestDTO(999L))))
                .andExpect(status().isBadRequest());

        // Approve with valid candidate
        mockMvc.perform(post("/admin/player-links/reviews/300/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.furbo.adapter.dto.ApproveRequestDTO(player.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action").value("LINKED"));

        assertThat(sourceRepository.findBySourceAndExternalId("WHOSCORED", "300")).isPresent();
    }

    @Test
    void test4_rejectCandidateAndRelink() throws Exception {
        Player player = new Player();
        player.setExternalId(50L);
        player.setName("Candidate Player");
        player.setTeam("Liverpool");
        playerRepository.save(player);

        createMockPageJson("page_1.json", 400L, "Mohamed Salah", "Liverpool", 800);
        importService.importLeague(tempLeagueDir.toString());

        PlayerMatchReview rev = PlayerMatchReview.builder()
            .source("WHOSCORED")
            .externalId("400")
            .candidatePlayerId(player.getId())
            .score(0.95)
            .reason("EXACT_NAME")
            .status(ReviewStatus.PENDING)
            .createdAt(java.time.LocalDateTime.now())
            .build();
        reviewRepository.save(rev);

        mockMvc.perform(post("/admin/player-links/reviews/400/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.furbo.adapter.dto.RejectRequestDTO(player.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action").value("REJECTED"));

        linkService.relinkAll();

        Optional<PlayerSourceId> link = sourceRepository.findBySourceAndExternalId("WHOSCORED", "400");
        assertThat(link).isEmpty();
    }

    @Test
    void test5_unlinkAndSubsequentRelink() throws Exception {
        Player player = new Player();
        player.setExternalId(60L);
        player.setName("Bukayo Saka");
        player.setTeam("Arsenal");
        playerRepository.save(player);

        createMockPageJson("page_1.json", 500L, "Bukayo Saka", "Arsenal", 1000);
        importService.importLeague(tempLeagueDir.toString());

        assertThat(sourceRepository.findBySourceAndExternalId("WHOSCORED", "500")).isPresent();

        mockMvc.perform(delete("/admin/player-links/links/500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action").value("UNLINKED"))
                .andExpect(jsonPath("$.previousPlayerId").value(player.getId()));

        assertThat(sourceRepository.findBySourceAndExternalId("WHOSCORED", "500")).isEmpty();
        List<PlayerSeasonStats> stats = statsRepository.findByWhoscoredPlayerId(500L);
        assertThat(stats.get(0).getPlayerId()).isNull();

        linkService.relinkAll();
        assertThat(sourceRepository.findBySourceAndExternalId("WHOSCORED", "500")).isEmpty();
    }

    @Test
    void test6_confirmLink() throws Exception {
        Player player = new Player();
        player.setExternalId(70L);
        player.setName("Erling Haaland");
        player.setTeam("Man City");
        playerRepository.save(player);

        createMockPageJson("page_1.json", 600L, "Erling Haaland", "Man City", 1200);
        importService.importLeague(tempLeagueDir.toString());

        Optional<PlayerSourceId> linkOpt = sourceRepository.findBySourceAndExternalId("WHOSCORED", "600");
        assertThat(linkOpt).isPresent();
        PlayerSourceId link = linkOpt.get();
        link.setNeedsReview(true);
        sourceRepository.save(link);

        mockMvc.perform(post("/admin/player-links/links/600/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action").value("CONFIRMED"));

        PlayerSourceId updated = sourceRepository.findBySourceAndExternalId("WHOSCORED", "600").get();
        assertThat(updated.isNeedsReview()).isFalse();

        mockMvc.perform(post("/admin/player-links/links/600/confirm"))
                .andExpect(status().isOk());
    }

    @Test
    void test7_saveReviewsAndRejectionsBehavior() throws Exception {
        Player p1 = new Player();
        p1.setExternalId(80L);
        p1.setName("Player One");
        playerRepository.save(p1);

        Player p2 = new Player();
        p2.setExternalId(81L);
        p2.setName("Player Two");
        playerRepository.save(p2);

        PlayerMatchReview rej = PlayerMatchReview.builder()
            .source("WHOSCORED")
            .externalId("700")
            .candidatePlayerId(p1.getId())
            .score(0.8)
            .reason("Test")
            .status(ReviewStatus.REJECTED)
            .createdAt(java.time.LocalDateTime.now())
            .build();
        reviewRepository.save(rej);

        List<MatchOutcome.Review.Candidate> candidates = List.of(
            new MatchOutcome.Review.Candidate(p1.getId(), 0.8, "Test"),
            new MatchOutcome.Review.Candidate(p2.getId(), 0.7, "Test")
        );

        persister.saveReviews("WHOSCORED", "700", candidates);

        List<PlayerMatchReview> revP1List = reviewRepository.findBySourceAndExternalIdAndCandidatePlayerId("WHOSCORED", "700", p1.getId());
        assertThat(revP1List).isNotEmpty();
        assertThat(revP1List.get(0).getStatus()).isEqualTo(ReviewStatus.REJECTED);

        List<PlayerMatchReview> revP2List = reviewRepository.findBySourceAndExternalIdAndCandidatePlayerId("WHOSCORED", "700", p2.getId());
        assertThat(revP2List).isNotEmpty();
        assertThat(revP2List.get(0).getStatus()).isEqualTo(ReviewStatus.PENDING);
    }

    @Test
    void test8_relinkIdempotencyManualLinksPreserved() throws Exception {
        Player player = new Player();
        player.setExternalId(90L);
        player.setName("Manual Guy");
        playerRepository.save(player);

        createMockPageJson("page_1.json", 800L, "Some Name", "Some Team", 400);
        importService.importLeague(tempLeagueDir.toString());

        adminService.linkManually("800", player.getId());

        linkService.relinkAll();
        linkService.relinkAll();

        PlayerSourceId link = sourceRepository.findBySourceAndExternalId("WHOSCORED", "800").get();
        assertThat(link.getMatchMethod()).isEqualTo("MANUAL");
    }
}
