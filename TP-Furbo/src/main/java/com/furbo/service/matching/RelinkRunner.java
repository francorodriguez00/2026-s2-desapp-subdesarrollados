package com.furbo.service.matching;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("whoscored-relink")
public class RelinkRunner implements CommandLineRunner {
    private final PlayerLinkService service;
    public RelinkRunner(PlayerLinkService service) { this.service = service; }
    @Override public void run(String... args) { service.relinkAll(); }
}
