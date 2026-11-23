package com.furbo.repository;

import com.furbo.config.TestcontainersConfig;
import com.furbo.model.Player;
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
public class PlayerRepositoryTest {

    @Autowired
    private PlayerRepository playerRepository;

    @Test
    void findByExternalIdIn_ReturnsPlayers() {
        Player player1 = new Player();
        player1.setExternalId(1L);
        player1.setName("Player 1");
        
        Player player2 = new Player();
        player2.setExternalId(2L);
        player2.setName("Player 2");

        playerRepository.saveAll(List.of(player1, player2));

        List<Player> found = playerRepository.findByExternalIdIn(List.of(1L, 2L));

        assertThat(found).hasSize(2);
    }
}
