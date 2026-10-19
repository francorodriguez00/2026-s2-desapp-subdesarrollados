package com.furbo.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class SimplePriceProvider implements PriceProvider {
    @Override
    public BigDecimal getCurrentPrice(Long playerId) {
        // Implementación temporal según requerimiento (Fase 3)
        return BigDecimal.valueOf(1);
    }
}
