package com.furbo.unit;

import com.furbo.controller.PlayerController;
import com.furbo.service.PlayerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlayerController.class)
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlayerService playerService;

    @Test
    @WithMockUser
    void getPlayers_ShouldReturn200() throws Exception {
        when(playerService.getPlayers()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/players"))
                .andExpect(status().isOk());
    }

    @Test
    void getPlayers_WithoutAuth_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/players"))
                .andExpect(status().isForbidden());
    }
}
