package com.furbo.service;

import com.furbo.model.PlayerQuote;
import com.furbo.repository.PlayerQuoteRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@Primary
public class QuotedPriceProvider implements PriceProvider {
    private final PlayerQuoteRepository quoteRepository;
    private final SimplePriceProvider fallback;

    public QuotedPriceProvider(PlayerQuoteRepository quoteRepository, SimplePriceProvider fallback) {
        this.quoteRepository = quoteRepository;
        this.fallback = fallback;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getCurrentPrice(Long playerId) {
        return quoteRepository.findFirstByPlayerIdOrderByCalculatedAtDescIdDesc(playerId)
                .map(PlayerQuote::getPrice)
                .orElseGet(() -> fallback.getCurrentPrice(playerId));
    }
}
