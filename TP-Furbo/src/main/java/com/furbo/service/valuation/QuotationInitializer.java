package com.furbo.service.valuation;

import com.furbo.model.QuoteTrigger;
import com.furbo.repository.PlayerQuoteRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(4)
public class QuotationInitializer implements CommandLineRunner {

    private final QuotationService service;
    private final PlayerQuoteRepository repository;
    private final boolean enabled;

    public QuotationInitializer(QuotationService service, 
                                PlayerQuoteRepository repository,
                                @Value("${furbo.valuation.recalculate-on-startup:true}") boolean enabled) {
        this.service = service;
        this.repository = repository;
        this.enabled = enabled;
    }

    @Override
    public void run(String... args) {
        if (enabled && repository.count() == 0) {
            try {
                service.recalculateAll(QuoteTrigger.STARTUP);
            } catch (Exception e) {
                // Logueo silencioso para no impedir arranque
            }
        }
    }
}
