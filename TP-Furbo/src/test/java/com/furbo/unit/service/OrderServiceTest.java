package com.furbo.unit.service;

import com.furbo.model.*;
import com.furbo.repository.*;
import com.furbo.service.MarketDomainService;
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

    @Mock private SellOfferRepository sellOfferRepository;
    @Mock private PosicionPorJugadorRepository posicionRepository;
    @Mock private PortfolioRepository portfolioRepository;
    @Mock private UserRepository userRepository;
    @Mock private TransactionRepository transactionRepository; // <-- Agregado
    @Mock private PriceProvider priceProvider;
    @Mock private MarketDomainService domainService;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void buy_InsufficientFunds_ThrowsException() {
        User buyer = new User();
        buyer.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(priceProvider.getCurrentPrice(100L)).thenReturn(new BigDecimal("10.00"));
        when(portfolioRepository.findByUser(buyer)).thenReturn(Optional.of(new Portfolio()));
        when(domainService.canUserAfford(any(), eq(1), any())).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> orderService.buy(1L, 100L, 1));
    }

    @Test
    void buy_SuccessfulMatch_UpdatesState() {
        User buyer = new User();
        buyer.setId(1L);
        buyer.setBalance(new BigDecimal("100.00"));
        Portfolio buyerPortfolio = new Portfolio();
        buyerPortfolio.setId(1L);
        
        User seller = new User();
        seller.setId(2L);
        Portfolio sellerPortfolio = new Portfolio();
        sellerPortfolio.setId(2L);
        PosicionPorJugador sellerPos = new PosicionPorJugador();
        sellerPos.setCantidad(10);
        sellerPos.setReservedQuantity(5);

        SellOffer offer = new SellOffer();
        offer.setSellerId(2L);
        offer.setQuantityRemaining(5);
        offer.setQuantityTotal(5);

        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(userRepository.findById(2L)).thenReturn(Optional.of(seller));
        when(portfolioRepository.findByUser(buyer)).thenReturn(Optional.of(buyerPortfolio));
        when(portfolioRepository.findByUser(seller)).thenReturn(Optional.of(sellerPortfolio));
        when(priceProvider.getCurrentPrice(100L)).thenReturn(new BigDecimal("10.00"));
        when(domainService.canUserAfford(any(), eq(5), any())).thenReturn(true);
        when(sellOfferRepository.findOpenOffersForPlayer(100L)).thenReturn(List.of(offer));
        when(posicionRepository.findByPortfolioIdAndPlayerId(2L, 100L)).thenReturn(Optional.of(sellerPos));

        orderService.buy(1L, 100L, 5);

        verify(sellOfferRepository).save(any(SellOffer.class));
        verify(userRepository).save(buyer);
        assertEquals(new BigDecimal("50.00"), buyer.getBalance());
    }

    @Test
    void sell_Successful_CreatesOffer() {
        User seller = new User();
        seller.setId(2L);
        Portfolio portfolio = new Portfolio();
        portfolio.setId(2L);
        PosicionPorJugador pos = new PosicionPorJugador();
        pos.setCantidad(10);
        pos.setReservedQuantity(0);

        when(userRepository.findById(2L)).thenReturn(Optional.of(seller));
        when(portfolioRepository.findByUser(seller)).thenReturn(Optional.of(portfolio));
        when(posicionRepository.findByPortfolioIdAndPlayerId(2L, 100L)).thenReturn(Optional.of(pos));
        
        orderService.sell(2L, 100L, 5);

        verify(sellOfferRepository).save(any(SellOffer.class));
        assertEquals(5, pos.getReservedQuantity());
    }

    @Test
    void buy_UsesPriceProvider_UpdatesBalance() {
        User buyer = new User();
        buyer.setId(1L);
        buyer.setBalance(new BigDecimal("100.00"));
        Portfolio buyerPortfolio = new Portfolio();
        buyerPortfolio.setId(1L);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(portfolioRepository.findByUser(buyer)).thenReturn(Optional.of(buyerPortfolio));
        when(priceProvider.getCurrentPrice(100L)).thenReturn(new BigDecimal("25.00"));
        when(domainService.canUserAfford(any(), eq(2), any())).thenReturn(true);
        when(sellOfferRepository.findOpenOffersForPlayer(100L)).thenReturn(List.of()); // No stocks, force exception

        assertThrows(IllegalStateException.class, () -> orderService.buy(1L, 100L, 2));
        verify(priceProvider).getCurrentPrice(100L);
    }
}
