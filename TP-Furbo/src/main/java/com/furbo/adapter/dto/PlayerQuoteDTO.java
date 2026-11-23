package com.furbo.adapter.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PlayerQuoteDTO(
    Long playerId,
    BigDecimal price,
    BigDecimal score,
    String strategyCode,
    Integer strategyVersion,
    LocalDateTime calculatedAt,
    LocalDateTime statsUpdatedAt
) {}
