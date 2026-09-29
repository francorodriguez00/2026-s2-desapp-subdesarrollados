package com.furbo.job;

import com.furbo.integration.football.FootballDataClient;
import com.furbo.integration.football.FootballDataMapper;
import com.furbo.repository.PlayerRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SyncCatalogJob {
    private final FootballDataClient client;
    private final FootballDataMapper mapper;
    private final PlayerRepository repository;

    public SyncCatalogJob(FootballDataClient client, FootballDataMapper mapper, PlayerRepository repository) {
        this.client = client;
        this.mapper = mapper;
        this.repository = repository;
    }

    @Scheduled(fixedDelay = 86400000)
    public void sync() {
        // Lógica de sincronización simplificada
    }
}
