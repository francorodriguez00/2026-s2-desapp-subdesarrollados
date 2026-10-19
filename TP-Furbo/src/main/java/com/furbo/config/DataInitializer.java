package com.furbo.config;

import com.furbo.service.PlayerService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {

    private final PlayerService playerService;

    public DataInitializer(PlayerService playerService) {
        this.playerService = playerService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // La carga inicial se disparará aquí. 
        // Posteriormente integraremos el nuevo fetch robusto.
        playerService.fetchAndSavePlayers();
    }
}
