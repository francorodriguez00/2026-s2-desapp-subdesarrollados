package com.furbo.integration;

import com.furbo.domain.model.Player;
import com.furbo.repository.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CatalogIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlayerRepository playerRepository;

    @Test
    @WithMockUser
    void testCatalogRetrieval() throws Exception {
        // Given
        Player player = Player.builder()
                .externalId(1L)
                .name("Test Player")
                .build();
        playerRepository.save(player);

        // When/Then
        mockMvc.perform(get("/players"))
                .andExpect(status().isOk());
    }
}
