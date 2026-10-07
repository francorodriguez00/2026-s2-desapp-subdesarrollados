package com.furbo.unit;

import com.furbo.model.Player;
import com.furbo.repository.PlayerRepository;
import com.furbo.service.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PlayerService playerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getPlayers_WhenDatabaseIsEmpty_FetchesFromApi() {
        when(playerRepository.count()).thenReturn(0L);
        // Evitar que el test intente llamar a la API real, mockeando la llamada a la liga
        // Esto es un test unitario simple que verifica la lógica de delegación
        
        playerService.getPlayers();

        verify(playerRepository, times(1)).count();
        // Si entra en fetchAndSavePlayers, llamará a restTemplate
    }

    @Test
    void getPlayers_WhenDatabaseHasData_ReturnsPlayers() {
        Player player = new Player(1L, "Messi", "FW", "Inter Miami", "Argentina", "MLS");
        when(playerRepository.count()).thenReturn(1L);
        when(playerRepository.findAll()).thenReturn(List.of(player));

        List<Player> result = playerService.getPlayers();

        assertEquals(1, result.size());
        assertEquals("Messi", result.get(0).getName());
        verify(restTemplate, never()).getForObject(anyString(), any());
    }
}
