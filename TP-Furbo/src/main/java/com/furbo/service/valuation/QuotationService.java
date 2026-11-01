package com.furbo.service.valuation;

import com.furbo.model.*;
import com.furbo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class QuotationService {
    private final StrategyConfigRepository configRepository;
    private final PlayerQuoteRepository quoteRepository;
    private final PlayerStatsSource statsSource;
    private final StrategyRegistry registry;

    public QuotationService(StrategyConfigRepository cr, PlayerQuoteRepository qr, PlayerStatsSource ss, StrategyRegistry sr) {
        this.configRepository = cr;
        this.quoteRepository = qr;
        this.statsSource = ss;
        this.registry = sr;
    }

    @Transactional
    public void recalculateAll(QuoteTrigger trigger) {
        StrategyConfig activeCfg = configRepository.findByActiveTrue()
                .orElseThrow(() -> new IllegalStateException("No active strategy config"));
        ValuationStrategy strategy = registry.get(activeCfg.getStrategyCode());

        for (PlayerStatsSnapshot s : statsSource.findAll()) {
            Optional<PlayerQuote> last = quoteRepository.findFirstByPlayerIdOrderByCalculatedAtDescIdDesc(s.playerId());
            if (last.isPresent() && last.get().getStrategyConfigId().equals(activeCfg.getId()) 
                && last.get().getStatsUpdatedAt().equals(s.updatedAt())) continue;

            QuoteResult res = strategy.evaluate(s, activeCfg.getConfigJson());
            if (res != null) {
                PlayerQuote q = PlayerQuote.builder()
                        .playerId(s.playerId())
                        .strategyCode(activeCfg.getStrategyCode())
                        .strategyVersion(activeCfg.getVersion())
                        .strategyConfigId(activeCfg.getId())
                        .score(res.score())
                        .price(res.price())
                        .statsUpdatedAt(s.updatedAt())
                        .calculatedAt(LocalDateTime.now())
                        .trigger(trigger)
                        .build();
                quoteRepository.save(q);
            }
        }
    }

    @Transactional
    public void activate(String code, Integer version) {
        configRepository.findByActiveTrue().ifPresent(c -> {
            c.setActive(false);
            configRepository.save(c);
        });
        configRepository.findByStrategyCodeAndVersion(code, version)
                .ifPresentOrElse(c -> {
                    c.setActive(true);
                    configRepository.save(c);
                }, () -> { throw new IllegalArgumentException("Version not found"); });
    }

    @Transactional
    public void createNewVersion(String code, StrategyConfigData data) {
        StrategyConfig last = configRepository.findTopByStrategyCodeOrderByVersionDesc(code)
                .orElseThrow(() -> new IllegalArgumentException("Strategy code not found"));
        StrategyConfig next = StrategyConfig.builder()
                .strategyCode(code)
                .version(last.getVersion() + 1)
                .configJson(data)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
        
        configRepository.findByActiveTrue().ifPresent(c -> {
            c.setActive(false);
            configRepository.save(c);
        });
        
        configRepository.save(next);
    }

    @Transactional
    public void activateLatest(String code) {
        StrategyConfig latest = configRepository.findTopByStrategyCodeOrderByVersionDesc(code)
                .orElseThrow(() -> new IllegalArgumentException("Strategy code not found: " + code));
        activate(code, latest.getVersion());
    }

    @Transactional(readOnly = true)
    public List<StrategyConfig> listAllConfigs() {
        return configRepository.findAll();
    }
}
