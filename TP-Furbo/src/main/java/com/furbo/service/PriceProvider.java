package com.furbo.service;

import java.math.BigDecimal;

public interface PriceProvider {
    BigDecimal getCurrentPrice(Long playerId);
}
