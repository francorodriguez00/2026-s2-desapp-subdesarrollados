package com.furbo.unit.mapping;

import com.furbo.model.Player;
import com.furbo.model.mapping.PlayerMapping;
import com.furbo.repository.mapping.PlayerMappingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class PlayerMappingRepositoryTest {

    @Autowired
    private PlayerMappingRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void testSaveAndFindMapping() {
        Player player = Player.builder()
                .externalId(100L)
                .name("Test Player")
                .build();
        entityManager.persist(player);

        PlayerMapping mapping = PlayerMapping.builder()
                .localPlayer(player)
                .whoScoredPlayerId(999L)
                .build();
        repository.save(mapping);

        var found = repository.findByWhoScoredPlayerId(999L);
        assertTrue(found.isPresent());
        assertEquals("Test Player", found.get().getLocalPlayer().getName());
    }
}
