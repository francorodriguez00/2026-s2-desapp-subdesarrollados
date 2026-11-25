package com.furbo.unit.service;

import com.furbo.model.*;
import com.furbo.model.enums.QuoteTrigger;
import com.furbo.model.quoteStrategy.StrategyRegistry;
import com.furbo.model.quoteStrategy.ValuationStrategy;
import com.furbo.repository.PlayerQuoteRepository;
import com.furbo.repository.StrategyConfigRepository;
import com.furbo.service.QuotationService;
import com.furbo.service.valuation.PlayerStatsSnapshot;
import com.furbo.service.valuation.PlayerStatsSource;
import com.furbo.service.valuation.QuoteResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuotationServiceTest {

    @Mock
    private StrategyConfigRepository configRepository;
    @Mock
    private PlayerQuoteRepository quoteRepository;
    @Mock
    private PlayerStatsSource statsSource;
    @Mock
    private StrategyRegistry registry;
    @Mock
    private ValuationStrategy strategy;

    @InjectMocks
    private QuotationService quotationService;

    @Test
    void recalculateAll_handlesNullPlayerIdSnapshotGracefully() {
        StrategyConfig config = StrategyConfig.builder()
                .id(1L)
                .strategyCode("TEST")
                .version(1)
                .active(true)
                .build();

        when(configRepository.findByActiveTrue()).thenReturn(Optional.of(config));
        when(registry.get("TEST")).thenReturn(strategy);

        PlayerStatsSnapshot nullIdSnapshot = new PlayerStatsSnapshot(
                null, "FW", 10, 500, 1, 1, 0, 0, 0, 7.0, 80.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                LocalDateTime.now()
        );

        PlayerStatsSnapshot validSnapshot = new PlayerStatsSnapshot(
                100L, "FW", 10, 500, 1, 1, 0, 0, 0, 7.0, 80.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                LocalDateTime.now()
        );

        when(statsSource.findAll()).thenReturn(List.of(nullIdSnapshot, validSnapshot));
        when(quoteRepository.findFirstByPlayerIdOrderByCalculatedAtDescIdDesc(100L)).thenReturn(Optional.empty());
        when(strategy.evaluate(eq(validSnapshot), any())).thenReturn(new QuoteResult(java.math.BigDecimal.TEN, new java.math.BigDecimal("1000.00"), null));

        quotationService.recalculateAll(QuoteTrigger.MANUAL);

        verify(quoteRepository, times(1)).save(any(PlayerQuote.class));
    }
}
