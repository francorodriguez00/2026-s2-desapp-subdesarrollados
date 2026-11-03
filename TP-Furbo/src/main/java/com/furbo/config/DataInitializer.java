package com.furbo.config;

import com.furbo.service.PlayerService;
import com.furbo.service.SuperuserTokenService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
@Profile("!whoscored-import")
@ConditionalOnProperty(name = "furbo.football-data.fetch-on-startup", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements ApplicationRunner {

    private final PlayerService playerService;
    private final SuperuserTokenService tokenService;

    public DataInitializer(PlayerService playerService, SuperuserTokenService tokenService) {
        this.playerService = playerService;
        this.tokenService = tokenService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        playerService.fetchAndSavePlayers();
        // tokenService.sincronizarTodos(); // Eliminado para evitar sincronización masiva en arranque
    }
}
