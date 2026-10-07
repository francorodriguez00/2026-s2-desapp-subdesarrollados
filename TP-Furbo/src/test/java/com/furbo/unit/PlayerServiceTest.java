package com.furbo.unit;

import com.furbo.adapter.dto.external.CompetitionTeamsDTO;
import com.furbo.adapter.dto.external.PlayerDTO;
import com.furbo.adapter.dto.external.TeamDTO;
import com.furbo.adapter.dto.external.TeamDetailDTO;
import com.furbo.model.Player;
import com.furbo.repository.PlayerRepository;
import com.furbo.service.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

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
    void getPlayers_WhenDatabaseIsEmpty_FetchesFromApi() throws InterruptedException {
        when(playerRepository.count()).thenReturn(0L);
        
        CompetitionTeamsDTO comp = new CompetitionTeamsDTO();
        TeamDTO team = new TeamDTO();
        team.setId(1L);
        comp.setTeams(List.of(team));
        
        when(restTemplate.getForObject(contains("/competitions/"), eq(CompetitionTeamsDTO.class)))
                .thenReturn(comp);
        
        TeamDetailDTO detail = new TeamDetailDTO();
        detail.setName("Team A");
        PlayerDTO p = new PlayerDTO();
        p.setName("Player A");
        detail.setSquad(List.of(p));
        
        when(restTemplate.getForObject(contains("/teams/1"), eq(TeamDetailDTO.class)))
                .thenReturn(detail);
        
        when(playerRepository.findAll()).thenReturn(List.of(
            Player.builder().name("Player A").build()
        ));
        
        List<Player> result = playerService.getPlayers();
        
        assertFalse(result.isEmpty());
        verify(playerRepository, times(1)).saveAll(anyList());
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
