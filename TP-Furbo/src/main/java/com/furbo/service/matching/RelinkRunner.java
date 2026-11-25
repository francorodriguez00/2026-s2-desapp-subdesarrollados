package com.furbo.service.matching;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("whoscored-relink")
public class RelinkRunner implements CommandLineRunner {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RelinkRunner.class);
    private final PlayerLinkService service;
    public RelinkRunner(PlayerLinkService service) { this.service = service; }
    @Override public void run(String... args) {
        var summary = service.relinkAll();
        log.info("Resumen relink: {}", summary);
    }
}
