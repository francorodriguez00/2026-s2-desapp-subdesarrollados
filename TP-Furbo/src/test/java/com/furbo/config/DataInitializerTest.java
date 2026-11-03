package com.furbo.config;

import com.furbo.service.PlayerService;
import com.furbo.service.SuperuserTokenService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class DataInitializerTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withBean(PlayerService.class, () -> Mockito.mock(PlayerService.class))
            .withBean(SuperuserTokenService.class, () -> Mockito.mock(SuperuserTokenService.class))
            .withUserConfiguration(DataInitializer.class);

    @Test
    void loadsWhenPropertyIsTrue() {
        runner.withPropertyValues("furbo.football-data.fetch-on-startup=true")
                .run(context -> assertThat(context).hasSingleBean(DataInitializer.class));
    }

    @Test
    void loadsWhenPropertyIsMissing() {
        runner.run(context -> assertThat(context).hasSingleBean(DataInitializer.class));
    }

    @Test
    void doesNotLoadWhenPropertyIsFalse() {
        runner.withPropertyValues("furbo.football-data.fetch-on-startup=false")
                .run(context -> assertThat(context).doesNotHaveBean(DataInitializer.class));
    }
}