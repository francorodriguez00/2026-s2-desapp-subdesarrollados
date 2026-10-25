package com.furbo.unit.service;

import com.furbo.model.*;
import com.furbo.repository.*;
import com.furbo.service.MarketDomainService;
import com.furbo.service.OrderService;
import com.furbo.service.PriceProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private SellOfferRepository sellOfferRepository;
    @Mock
    private PosicionPorJugadorRepository posicionRepository;
    @Mock
    private PortfolioRepository portfolioRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PriceProvider priceProvider;
    @Mock
    private MarketDomainService domainService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void sell_ValidInput_CreatesOffer() {
        User user = new User();
        user.setId(1L);
        Portfolio portfolio = new Portfolio();
        portfolio.setId(1L);
        PosicionPorJugador posicion = new PosicionPorJugador();
        posicion.setCantidad(10);
        posicion.setReservedQuantity(0);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(portfolioRepository.findByUser(user)).thenReturn(Optional.of(portfolio));
        when(posicionRepository.findByPortfolioIdAndPlayerId(1L, 1L)).thenReturn(Optional.of(posicion));

        orderService.sell(1L, 1L, 5);

        verify(sellOfferRepository).save(any(SellOffer.class));
        verify(posicionRepository).save(posicion);
    }

    @Test
    void sell_NoPortfolio_ThrowsException() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(portfolioRepository.findByUser(user)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> orderService.sell(1L, 1L, 5));
    }
}
