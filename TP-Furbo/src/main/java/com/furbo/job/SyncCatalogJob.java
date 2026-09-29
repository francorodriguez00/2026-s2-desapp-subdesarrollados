package com.furbo.job;

import com.furbo.domain.model.Player;
import com.furbo.integration.football.FootballDataProvider;
import com.furbo.integration.football.FootballDataMapper;
import com.furbo.repository.PlayerRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SyncCatalogJob {
    private final FootballDataProvider client;
    private final FootballDataMapper mapper;
    private final PlayerRepository repository;

    public SyncCatalogJob(FootballDataProvider client, FootballDataMapper mapper, PlayerRepository repository) {
        this.client = client;
        this.mapper = mapper;
        this.repository = repository;
    }

    @PostConstruct
    @Scheduled(fixedDelay = 86400000)
    public void sync() {
        System.out.println("Iniciando carga de jugadores desde la API...");

        // PL = Inglaterra, BL1 = Alemania, PD = España, SA = Italia, FL1 = Francia
        List<String> leagues = List.of("PL", "BL1", "PD", "SA", "FL1");

        for (String league : leagues) {
            System.out.println("Extrayendo datos de: " + league);

            List<Player> players = client.getPlayersForLeague(league).stream()
                    .map(mapper::toDomain)
                    .collect(Collectors.toList());

            if (!players.isEmpty()) {
                repository.saveAll(players);
                System.out.println("Guardados " + players.size() + " jugadores de " + league);
            }

            // Pausa obligatoria para que la API gratuita no nos bloquee (Rate Limit)
            try { Thread.sleep(6000); } catch (InterruptedException ignored) {}
        }
        System.out.println("Carga inicial completa. Podés consultar el Swagger.");
    }
}