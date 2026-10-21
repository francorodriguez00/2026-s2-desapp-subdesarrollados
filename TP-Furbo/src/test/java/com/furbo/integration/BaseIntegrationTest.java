package com.furbo.integration;

import com.furbo.config.TestcontainersConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfig.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public abstract class BaseIntegrationTest { }
