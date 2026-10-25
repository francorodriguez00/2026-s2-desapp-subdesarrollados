package com.furbo.unit.service;

import com.furbo.model.User;
import com.furbo.service.MarketDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class MarketDomainServiceTest {

    private MarketDomainService marketDomainService;
    private User buyer;

    @BeforeEach
    void setUp() {
        marketDomainService = new MarketDomainService();
        buyer = new User();
    }

    @Test
    void canUserAfford_SufficientBalance_ReturnsTrue() {
        buyer.setBalance(new BigDecimal("100.00"));
        assertTrue(marketDomainService.canUserAfford(buyer, 2, new BigDecimal("40.00")));
    }

    @Test
    void canUserAfford_ExactBalance_ReturnsTrue() {
        buyer.setBalance(new BigDecimal("80.00"));
        assertTrue(marketDomainService.canUserAfford(buyer, 2, new BigDecimal("40.00")));
    }

    @Test
    void canUserAfford_InsufficientBalance_ReturnsFalse() {
        buyer.setBalance(new BigDecimal("70.00"));
        assertFalse(marketDomainService.canUserAfford(buyer, 2, new BigDecimal("40.00")));
    }

    @Test
    void validateSellQuantity_SufficientStock_DoesNotThrow() {
        assertDoesNotThrow(() -> marketDomainService.validateSellQuantity(10, 2, 3));
    }

    @Test
    void validateSellQuantity_InsufficientStock_ThrowsException() {
        assertThrows(IllegalStateException.class, () -> marketDomainService.validateSellQuantity(10, 9, 2));
    }
}
