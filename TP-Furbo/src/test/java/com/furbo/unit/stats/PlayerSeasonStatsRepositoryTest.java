package com.furbo.unit.stats;

import com.furbo.model.Player;
import com.furbo.model.stats.PlayerSeasonStats;
import com.furbo.repository.stats.PlayerSeasonStatsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class PlayerSeasonStatsRepositoryTest {

    @Autowired
    private PlayerSeasonStatsRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void testSaveAndFindStats() {
        Player player = Player.builder()
                .externalId(100L)
                .name("John Doe")
                .build();
        entityManager.persist(player);

        PlayerSeasonStats stats = PlayerSeasonStats.builder()
                .player(player)
                .whoScoredPlayerId(999L)
                .seasonId("11141")
                .competition("Premier League")
                .apps(10)
                .rating(7.5)
                .build();
        repository.save(stats);

        var found = repository.findByWhoScoredPlayerIdAndSeasonIdAndCompetition(999L, "11141", "Premier League");
        assertTrue(found.isPresent());
        assertEquals(7.5, found.get().getRating());
    }
}
