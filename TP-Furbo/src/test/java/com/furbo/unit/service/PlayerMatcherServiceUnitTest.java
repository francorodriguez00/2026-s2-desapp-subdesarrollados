package com.furbo.unit.service;

import com.furbo.model.Player;
import com.furbo.service.matching.*;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class PlayerMatcherServiceUnitTest {

    @Test
    void testLearnTeamMapPure() {
        List<Object[]> votes = List.of(
            new Object[]{"PSG", "Paris Saint-Germain FC", 5L},
            new Object[]{"PSG", "Other FC", 1L},
            new Object[]{"Real", "Real Madrid", 2L}, // below minVotes=3
            new Object[]{"Barca", "Barcelona B", 2L},
            new Object[]{"Barca", "Barcelona A", 4L} // tie breaker / majority
        );

        Map<String, String> teamMap = PlayerMatcherService.learnTeamMap(votes, 3, 0.6);
        assertThat(teamMap).containsEntry("psg", "paris saint germain");
        assertThat(teamMap).doesNotContainKey("real");
        assertThat(teamMap).containsEntry("barca", "barcelona a");
    }

    @Test
    void testMatchFuzzyScenarios() {
        Player p1 = new Player();
        p1.setId(1L);
        p1.setName("Kylian Mbappe");
        p1.setTeam("Paris Saint-Germain FC");
        p1.setPosition("Forward");
        p1.setLeague("Ligue 1");

        Player p2 = new Player();
        p2.setId(2L);
        p2.setName("Kylian Mbappe Lottin");
        p2.setTeam("Paris Saint-Germain FC");
        p2.setPosition("Forward");
        p2.setLeague("Ligue 1");

        Player linkedP = new Player();
        linkedP.setId(3L);
        linkedP.setName("Linked Guy");
        linkedP.setTeam("Paris Saint-Germain FC");

        Map<String, List<Player>> byName = Map.of("kylian mbappe", List.of(p1, p2));
        Map<String, List<Player>> byTeam = Map.of(
            NameNormalizer.normalizeTeam(p1.getTeam()), List.of(p1, p2, linkedP)
        );
        Map<Long, Player> byId = Map.of(1L, p1, 2L, p2, 3L, linkedP);
        Map<String, String> teamMap = Map.of("psg", NameNormalizer.normalizeTeam(p1.getTeam()));
        Map<Integer, String> tournMap = Map.of(100, "Ligue 1");
        Set<Long> linked = new HashSet<>(List.of(3L)); // linkedPlayerIds (p3 is linked, should not appear as candidate)

        MatchContext ctx = new MatchContext(byName, byTeam, byId, teamMap, tournMap, linked);
        FurboMatchingProperties props = new FurboMatchingProperties();
        PlayerMatcherService service = new PlayerMatcherService(null, null, null, props);

        // 1. Two strong candidates give Review
        WhoScoredPlayerRef ref1 = new WhoScoredPlayerRef("10", "Kylian Mbappe", "Kylian", "Mbappe", List.of("PSG"), List.of(100), "Forward");
        MatchOutcome outcomeReview = service.matchFuzzy(ref1, ctx);
        assertThat(outcomeReview).isInstanceOf(MatchOutcome.Review.class);

        // If only p1 is present (single strong candidate), links FUZZY_TEAM
        Map<String, List<Player>> byTeamSingle = Map.of(
            NameNormalizer.normalizeTeam(p1.getTeam()), List.of(p1, linkedP)
        );
        MatchContext ctxSingle = new MatchContext(byName, byTeamSingle, byId, teamMap, tournMap, linked);
        MatchOutcome outcomeSingle = service.matchFuzzy(ref1, ctxSingle);
        assertThat(outcomeSingle).isInstanceOf(MatchOutcome.Linked.class);
        assertThat(((MatchOutcome.Linked) outcomeSingle).method()).isEqualTo("FUZZY_TEAM");
        assertThat(((MatchOutcome.Linked) outcomeSingle).playerId()).isEqualTo(1L);

        // 2. Pool from league (not team) gives Review and never links
        WhoScoredPlayerRef refLeague = new WhoScoredPlayerRef("11", "Kylian Mbappe", "Kylian", "Mbappe", List.of("UnknownTeam"), List.of(100), "Forward");
        MatchOutcome outcomeLeague = service.matchFuzzy(refLeague, ctxSingle);
        assertThat(outcomeLeague).isInstanceOf(MatchOutcome.Review.class);

        // 3. Without candidates gives Unmatched
        WhoScoredPlayerRef refNone = new WhoScoredPlayerRef("12", "Nonexistent Player", "None", "Player", List.of("Unknown"), List.of(999), "Forward");
        MatchOutcome outcomeNone = service.matchFuzzy(refNone, ctxSingle);
        assertThat(outcomeNone).isInstanceOf(MatchOutcome.Unmatched.class);
    }

    @Test
    void testMatchExactHomonymsAndEmptyTeam() {
        Player p1 = new Player();
        p1.setId(1L);
        p1.setName("Homonym");
        p1.setTeam("Team A");

        Player p2 = new Player();
        p2.setId(2L);
        p2.setName("Homonym");
        p2.setTeam("Team B");

        Map<String, List<Player>> byName = Map.of("homonym", List.of(p1, p2));
        MatchContext ctx = new MatchContext(byName, new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashSet<>());
        PlayerMatcherService service = new PlayerMatcherService(null, null, null, new FurboMatchingProperties());

        // Empty team name in ref should give Review (homonyms without matching team), not matching all via contains("")
        WhoScoredPlayerRef refEmptyTeam = new WhoScoredPlayerRef("20", "Homonym", "H", "O", List.of(""), List.of(), "M");
        MatchOutcome outcomeEmpty = service.matchExact(refEmptyTeam, ctx);
        assertThat(outcomeEmpty).isInstanceOf(MatchOutcome.Review.class);

        // Multiple teamNames in ref checked against candidate
        WhoScoredPlayerRef refMultipleTeams = new WhoScoredPlayerRef("21", "Homonym", "H", "O", List.of("WrongTeam", "Team B"), List.of(), "M");
        MatchOutcome outcomeMulti = service.matchExact(refMultipleTeams, ctx);
        assertThat(outcomeMulti).isInstanceOf(MatchOutcome.Linked.class);
        assertThat(((MatchOutcome.Linked) outcomeMulti).playerId()).isEqualTo(2L);
    }
}
