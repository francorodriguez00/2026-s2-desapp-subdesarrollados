package com.furbo.service;

import com.furbo.model.Player;
import com.furbo.repository.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.util.Arrays;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class PlayerMatcherServiceTest {
    @Test
    public void testFindMatch_ExactName() {
        PlayerRepository repo = Mockito.mock(PlayerRepository.class);
        Player p = Player.builder().id(1L).name("Iliman Ndiaye").build();
        when(repo.findAll()).thenReturn(Arrays.asList(p));
        
        PlayerMatcherService service = new PlayerMatcherService(repo);
        Optional<Player> result = service.findMatch("Iliman Ndiaye", "Everton");
        
        assertTrue(result.isPresent());
    }
}
