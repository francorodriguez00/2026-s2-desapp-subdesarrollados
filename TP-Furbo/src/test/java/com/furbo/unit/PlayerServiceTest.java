package com.furbo.unit;

import com.furbo.model.Player;
import com.furbo.repository.PlayerRepository;
import com.furbo.service.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PlayerService playerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getPlayers_WhenDatabaseIsEmpty_ReturnsEmptyList() {
        when(playerRepository.count()).thenReturn(0L);
        when(playerRepository.findAll()).thenReturn(Collections.emptyList());

        List<Player> result = playerService.getPlayers();

        assertTrue(result.isEmpty());
        verify(playerRepository, times(1)).count();
    }

    @Test
    void getPlayers_WhenDatabaseHasData_ReturnsPlayers() {
        Player player = new Player(1L, "Messi", "FW", "Inter Miami", "Argentina", "MLS");
        when(playerRepository.count()).thenReturn(1L);
        when(playerRepository.findAll()).thenReturn(List.of(player));

        List<Player> result = playerService.getPlayers();

        assertEquals(1, result.size());
        assertEquals("Messi", result.get(0).getName());
    }
}
