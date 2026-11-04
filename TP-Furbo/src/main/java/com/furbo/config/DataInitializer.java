package com.furbo.config;

import com.furbo.service.PlayerService;
import com.furbo.service.SuperuserTokenService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
@Profile("!whoscored-import")
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
