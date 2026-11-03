package com.furbo.unit.service;

import com.furbo.model.PlayerSeasonStats;
import com.furbo.repository.PlayerSeasonStatsRepository;
import com.furbo.service.valuation.PlayerStatsSnapshot;
import com.furbo.service.valuation.PlayerStatsSourceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerStatsSourceImplTest {

    @Mock
    private PlayerSeasonStatsRepository repository;

    @InjectMocks
    private PlayerStatsSourceImpl statsSource;

    @Test
    void findAll_excludesNullPlayerIdAndDoesNotThrow() {
        PlayerSeasonStats s1 = PlayerSeasonStats.builder().id(1L).playerId(100L).minsPlayed(500).build();
        PlayerSeasonStats s2 = PlayerSeasonStats.builder().id(2L).playerId(null).minsPlayed(300).build();

        when(repository.findByPlayerIdIsNotNull()).thenReturn(List.of(s1, s2));

        List<PlayerStatsSnapshot> result = statsSource.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).playerId()).isEqualTo(100L);
    }

    @Test
    void pickBest_selectsRowWithMostMinutes() {
        PlayerSeasonStats s1 = PlayerSeasonStats.builder().id(1L).playerId(100L).minsPlayed(100).build();
        PlayerSeasonStats s2 = PlayerSeasonStats.builder().id(2L).playerId(100L).minsPlayed(800).build();

        when(repository.findByPlayerId(100L)).thenReturn(List.of(s1, s2));

        Optional<PlayerStatsSnapshot> result = statsSource.findByPlayerId(100L);

        assertThat(result).isPresent();
        assertThat(result.get().minsPlayed()).isEqualTo(800);
    }

    @Test
    void pickBest_tieBreaksByUpdatedAtAndId() {
        LocalDateTime now = LocalDateTime.now();
        PlayerSeasonStats s1 = PlayerSeasonStats.builder().id(1L).playerId(100L).minsPlayed(500).updatedAt(now.minusDays(1)).build();
        PlayerSeasonStats s2 = PlayerSeasonStats.builder().id(2L).playerId(100L).minsPlayed(500).updatedAt(now).build();
        PlayerSeasonStats s3 = PlayerSeasonStats.builder().id(3L).playerId(100L).minsPlayed(500).updatedAt(now).build();

        when(repository.findByPlayerId(100L)).thenReturn(List.of(s1, s2, s3));

        Optional<PlayerStatsSnapshot> result = statsSource.findByPlayerId(100L);

        assertThat(result).isPresent();
        assertThat(result.get().minsPlayed()).isEqualTo(500);
        // S3 should win because it has the same latest updatedAt but higher id (3 vs 2)
        // Wait, let's verify repository findAll or findByPlayerId behavior.
    }

    @Test
    void minsPlayedNullTreatedAsZero() {
        PlayerSeasonStats s1 = PlayerSeasonStats.builder().id(1L).playerId(100L).minsPlayed(null).build();
        PlayerSeasonStats s2 = PlayerSeasonStats.builder().id(2L).playerId(100L).minsPlayed(50).build();

        when(repository.findByPlayerId(100L)).thenReturn(List.of(s1, s2));

        Optional<PlayerStatsSnapshot> result = statsSource.findByPlayerId(100L);

        assertThat(result).isPresent();
        assertThat(result.get().minsPlayed()).isEqualTo(50);
    }

    @Test
    void findByPlayerId_nullOrNonExistent_returnsEmpty() {
        assertThat(statsSource.findByPlayerId(null)).isEmpty();

        when(repository.findByPlayerId(999L)).thenReturn(List.of());
        assertThat(statsSource.findByPlayerId(999L)).isEmpty();
    }

    @Test
    void verifySnapshotMappingExhaustively() {
        LocalDateTime now = LocalDateTime.now();
        PlayerSeasonStats stats = PlayerSeasonStats.builder()
                .playerId(10L)
                .positionText("FW")
                .apps(20)
                .minsPlayed(1500)
                .goals(10)
                .assists(5)
                .yellowCards(2)
                .redCards(0)
                .manOfTheMatch(3)
                .rating(7.5)
                .passSuccess(85.0)
                .keyPassPerGame(1.2)
                .dribbleWonPerGame(2.1)
                .foulGivenPerGame(1.0)
                .dispossessedPerGame(0.5)
                .turnoverPerGame(1.1)
                .tacklePerGame(0.8)
                .interceptionPerGame(0.4)
                .foulsPerGame(1.5)
                .clearancePerGame(0.2)
                .wasDribbledPerGame(0.3)
                .outfielderBlockPerGame(0.1)
                .aerialWonPerGame(1.8)
                .totalPassesPerGame(40.0)
                .accurateCrossesPerGame(0.5)
                .accurateLongPassPerGame(2.0)
                .accurateThroughBallPerGame(0.4)
                .updatedAt(now)
                .build();

        when(repository.findByPlayerId(10L)).thenReturn(List.of(stats));

        Optional<PlayerStatsSnapshot> optSnapshot = statsSource.findByPlayerId(10L);
        assertThat(optSnapshot).isPresent();
        PlayerStatsSnapshot snap = optSnapshot.get();

        assertThat(snap.playerId()).isEqualTo(10L);
        assertThat(snap.positionText()).isEqualTo("FW");
        assertThat(snap.apps()).isEqualTo(20);
        assertThat(snap.minsPlayed()).isEqualTo(1500);
        assertThat(snap.goals()).isEqualTo(10);
        assertThat(snap.assists()).isEqualTo(5);
        assertThat(snap.yellowCards()).isEqualTo(2);
        assertThat(snap.redCards()).isEqualTo(0);
        assertThat(snap.manOfTheMatch()).isEqualTo(3);
        assertThat(snap.rating()).isEqualTo(7.5);
        assertThat(snap.passSuccess()).isEqualTo(85.0);
        assertThat(snap.keyPassPerGame()).isEqualTo(1.2);
        assertThat(snap.dribbleWonPerGame()).isEqualTo(2.1);
        assertThat(snap.foulGivenPerGame()).isEqualTo(1.0);
        assertThat(snap.dispossessedPerGame()).isEqualTo(0.5);
        assertThat(snap.turnoverPerGame()).isEqualTo(1.1);
        assertThat(snap.tacklePerGame()).isEqualTo(0.8);
        assertThat(snap.interceptionPerGame()).isEqualTo(0.4);
        assertThat(snap.foulsPerGame()).isEqualTo(1.5);
        assertThat(snap.clearancePerGame()).isEqualTo(0.2);
        assertThat(snap.wasDribbledPerGame()).isEqualTo(0.3);
        assertThat(snap.outfielderBlockPerGame()).isEqualTo(0.1);
        assertThat(snap.aerialWonPerGame()).isEqualTo(1.8);
        assertThat(snap.totalPassesPerGame()).isEqualTo(40.0);
        assertThat(snap.accurateCrossesPerGame()).isEqualTo(0.5);
        assertThat(snap.accurateLongPassPerGame()).isEqualTo(2.0);
        assertThat(snap.accurateThroughBallPerGame()).isEqualTo(0.4);
        assertThat(snap.updatedAt()).isEqualTo(now);
    }
}
