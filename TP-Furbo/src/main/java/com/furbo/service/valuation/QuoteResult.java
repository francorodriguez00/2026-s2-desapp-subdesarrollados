package com.furbo.service.valuation;

import java.math.BigDecimal;

public record QuoteResult(
    BigDecimal score,
    BigDecimal price,
    PositionGroup group
) {}
