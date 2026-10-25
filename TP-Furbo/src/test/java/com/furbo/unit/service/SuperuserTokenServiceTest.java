package com.furbo.unit.service;

import com.furbo.model.*;
import com.furbo.repository.*;
import com.furbo.service.SuperuserTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SuperuserTokenServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PortfolioRepository portfolioRepository;
    @Mock private PosicionPorJugadorRepository posicionRepository;
    @Mock private PlayerRepository playerRepository;

    @InjectMocks
    private SuperuserTokenService superuserTokenService;

    private User superuser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        superuser = new User();
        superuser.setId(1L);
    }

    @Test
    void asegurarPortfolioSuperuser_ExistingPortfolio_ReturnsPortfolio() {
        when(userRepository.findByIsSuperuserTrue()).thenReturn(Optional.of(superuser));
        Portfolio portfolio = new Portfolio();
        when(portfolioRepository.findByUser(superuser)).thenReturn(Optional.of(portfolio));

        Portfolio result = superuserTokenService.asegurarPortfolioSuperuser();

        assertEquals(portfolio, result);
    }

    @Test
    void asegurarPortfolioSuperuser_NoPortfolio_CreatesOne() {
        when(userRepository.findByIsSuperuserTrue()).thenReturn(Optional.of(superuser));
        when(portfolioRepository.findByUser(superuser)).thenReturn(Optional.empty());
        when(portfolioRepository.save(any(Portfolio.class))).thenAnswer(i -> i.getArgument(0));

        Portfolio result = superuserTokenService.asegurarPortfolioSuperuser();

        assertNotNull(result);
        verify(portfolioRepository).save(any(Portfolio.class));
    }

    @Test
    void asignarTokens_NewPlayer_Assigns100() {
        Portfolio portfolio = new Portfolio();
        portfolio.setId(1L);
        when(userRepository.findByIsSuperuserTrue()).thenReturn(Optional.of(superuser));
        when(portfolioRepository.findByUser(superuser)).thenReturn(Optional.of(portfolio));
        
        Player player = new Player();
        player.setExternalId(10L);
        when(posicionRepository.findByPortfolioIdAndPlayerId(1L, 10L)).thenReturn(Optional.empty());

        superuserTokenService.asignarTokens(player);

        verify(posicionRepository).save(any(PosicionPorJugador.class));
    }
}
