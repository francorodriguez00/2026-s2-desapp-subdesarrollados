package com.furbo.unit.service;

import com.furbo.model.*;
import com.furbo.repository.*;
import com.furbo.service.OrderService;
import com.furbo.service.PriceProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private TradeRepository tradeRepository;
    @Mock private UserRepository userRepository;
    @Mock private PortfolioRepository portfolioRepository;
    @Mock private PosicionPorJugadorRepository posicionRepository;
    @Mock private PlayerRepository playerRepository;
    @Mock private PriceProvider priceProvider;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createOrder_InsufficientFunds_ThrowsException() {
        User buyer = new User();
        buyer.setId(1L);
        buyer.setBalance(new BigDecimal("5.00"));
        buyer.setReservedBalance(BigDecimal.ZERO);

        when(userRepository.findByIdWithLock(1L)).thenReturn(Optional.of(buyer));
        when(playerRepository.findByIdWithLock(100L)).thenReturn(Optional.of(new Player()));
        when(priceProvider.getCurrentPrice(100L)).thenReturn(new BigDecimal("10.00"));
        when(portfolioRepository.findByUserWithLock(buyer)).thenReturn(Optional.of(new Portfolio()));

        assertThrows(IllegalStateException.class, () -> 
            orderService.createOrder(1L, 100L, Order.OrderSide.BUY, Order.OrderType.LIMIT, new BigDecimal("10.00"), 1)
        );
    }

    @Test
    void createOrder_PriceOutOfBounds_ThrowsException() {
        User buyer = new User();
        buyer.setId(1L);
        buyer.setBalance(new BigDecimal("1000.00"));

        when(playerRepository.findByIdWithLock(100L)).thenReturn(Optional.of(new Player()));
        when(userRepository.findByIdWithLock(1L)).thenReturn(Optional.of(buyer));
        when(priceProvider.getCurrentPrice(100L)).thenReturn(new BigDecimal("10.00"));

        // Price $20.00 is > 15.00 (max +50%)
        assertThrows(IllegalArgumentException.class, () -> 
            orderService.createOrder(1L, 100L, Order.OrderSide.BUY, Order.OrderType.LIMIT, new BigDecimal("20.00"), 1)
        );
    }
}
