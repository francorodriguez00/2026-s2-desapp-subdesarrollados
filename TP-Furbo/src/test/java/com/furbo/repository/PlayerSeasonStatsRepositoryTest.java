package com.furbo.repository;

import com.furbo.config.TestcontainersConfig;
import com.furbo.model.PlayerSeasonStats;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfig.class)
public class PlayerSeasonStatsRepositoryTest {

    @Autowired
    private PlayerSeasonStatsRepository repository;

    @Test
    void findByPlayerIdAndIsNotNull_WorksAsExpected() {
        PlayerSeasonStats s1 = PlayerSeasonStats.builder()
                .whoscoredPlayerId(1L)
                .whoscoredTeamId(1)
                .tournamentId(1)
                .seasonId(1)
                .playerId(100L)
                .build();

        PlayerSeasonStats s2 = PlayerSeasonStats.builder()
                .whoscoredPlayerId(2L)
                .whoscoredTeamId(1)
                .tournamentId(1)
                .seasonId(1)
                .playerId(null)
                .build();

        repository.saveAll(List.of(s1, s2));

        List<PlayerSeasonStats> forPlayer100 = repository.findByPlayerId(100L);
        assertThat(forPlayer100).hasSize(1);
        assertThat(forPlayer100.get(0).getPlayerId()).isEqualTo(100L);

        List<PlayerSeasonStats> notNullRows = repository.findByPlayerIdIsNotNull();
        assertThat(notNullRows).hasSize(1);
        assertThat(notNullRows.get(0).getPlayerId()).isNotNull();
    }
}
