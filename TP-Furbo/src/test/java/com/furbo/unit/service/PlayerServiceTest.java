package com.furbo.unit.service;

import com.furbo.adapter.dto.external.CompetitionTeamsDTO;
import com.furbo.adapter.dto.external.PlayerDTO;
import com.furbo.adapter.dto.external.TeamDTO;
import com.furbo.adapter.dto.external.TeamDetailDTO;
import com.furbo.model.Player;
import com.furbo.repository.PlayerRepository;
import com.furbo.service.PlayerService;
import com.furbo.service.SuperuserTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;
import com.furbo.exceptions.InvalidFilterException;
import com.furbo.model.enums.League;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PlayerServiceTest {

    @Mock private PlayerRepository playerRepository;
    @Mock private RestTemplate restTemplate;
    @Mock private SuperuserTokenService tokenService;

    private PlayerService playerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        playerService = new PlayerService(playerRepository, restTemplate, tokenService, 0, 0);
    }

    @Test
    void fetchAndSavePlayers_guardaJugadoresYAsignaTokens() {
        CompetitionTeamsDTO comp = new CompetitionTeamsDTO();
        TeamDTO team = new TeamDTO();
        team.setId(1L);
        comp.setTeams(List.of(team));

        TeamDetailDTO detail = new TeamDetailDTO();
        detail.setName("Team A");
        PlayerDTO p = new PlayerDTO();
        p.setId(10L);                 
        p.setName("Player A");
        detail.setSquad(List.of(p));

        when(restTemplate.getForObject(contains("/competitions/"), eq(CompetitionTeamsDTO.class)))
                .thenReturn(comp);
        when(restTemplate.getForObject(contains("/teams/1"), eq(TeamDetailDTO.class)))
                .thenReturn(detail);

        playerService.fetchAndSavePlayers();

        verify(playerRepository, atLeastOnce()).saveAll(anyList());
        verify(tokenService, atLeastOnce()).asignarTokens(any(Player.class));
    }

    @Test
    void fetchAndSavePlayers_conDetalleDeEquipoNulo_noAsignaTokens() {
        CompetitionTeamsDTO comp = new CompetitionTeamsDTO();
        TeamDTO team = new TeamDTO();
        team.setId(1L);
        comp.setTeams(List.of(team));
        
        when(restTemplate.getForObject(contains("/competitions/"), eq(CompetitionTeamsDTO.class)))
                .thenReturn(comp);
        when(restTemplate.getForObject(contains("/teams/1"), eq(TeamDetailDTO.class)))
                .thenReturn(null);

        playerService.fetchAndSavePlayers();

        verifyNoInteractions(tokenService);
    }

    @Test
    void getPlayers_sinFiltros_delegaAlRepositorioYDevuelveLaPagina() {
        Player player = Player.builder().name("Messi").build();
        Pageable pageable = Pageable.unpaged();
        when(playerRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(player)));

        Page<Player> result = playerService.getPlayers(null, null, null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Messi", result.getContent().get(0).getName());
    }

    @Test
    void getPlayers_conLigaInvalida_lanzaExcepcionYNoConsultaElRepo() {
        InvalidFilterException ex = assertThrows(InvalidFilterException.class,
                () -> playerService.getPlayers("INVALID_LEAGUE", null, null, Pageable.unpaged()));

        assertEquals("La liga solicitada no existe", ex.getMessage());
        verifyNoInteractions(playerRepository);
    }

    @Test
    void getPlayers_conPosicionInvalida_lanzaExcepcionYNoConsultaElRepo() {
        InvalidFilterException ex = assertThrows(InvalidFilterException.class,
                () -> playerService.getPlayers(null, null, "INVALID_POSITION", Pageable.unpaged()));

        assertEquals("La posición solicitada no existe", ex.getMessage());
        verifyNoInteractions(playerRepository);
    }

    @Test
    void getPlayers_conCampoDeOrdenInvalido_lanzaExcepcionYNoConsultaElRepo() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("invalid_field"));

        InvalidFilterException ex = assertThrows(InvalidFilterException.class,
                () -> playerService.getPlayers(null, null, null, pageable));

        assertTrue(ex.getMessage().contains("invalid_field"));
        verifyNoInteractions(playerRepository);
    }

    @Test
    void getPlayers_conCampoDeOrdenValido_consultaElRepo() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
        when(playerRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(Page.empty());

        assertDoesNotThrow(() -> playerService.getPlayers(null, null, null, pageable));

        verify(playerRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getPlayers_conFiltrosValidos_consultaElRepoConElMismoPageable() {
        Pageable pageable = PageRequest.of(2, 5);
        when(playerRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(Page.empty());

        // League.values()[0] evita adivinar qué strings acepta parse()
        String liga = League.values()[0].name();

        assertDoesNotThrow(() -> playerService.getPlayers(liga, "  Barcelona  ", null, pageable));

        verify(playerRepository).findAll(any(Specification.class), eq(pageable));
    }
}
