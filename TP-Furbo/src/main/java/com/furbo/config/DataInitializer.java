package com.furbo.config;

import com.furbo.service.PlayerService;
import com.furbo.service.SuperuserTokenService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
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
        tokenService.sincronizarTodos();
    }
}
