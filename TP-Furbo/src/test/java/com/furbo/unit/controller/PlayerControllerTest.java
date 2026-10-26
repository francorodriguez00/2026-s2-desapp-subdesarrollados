package com.furbo.unit.controller;

import com.furbo.config.SecurityConfig;
import com.furbo.controller.PlayerController;
import com.furbo.security.JwtAuthenticationFilter;
import com.furbo.security.JwtService;
import com.furbo.service.PlayerService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.furbo.controller.GlobalExceptionHandler;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(
    controllers = {PlayerController.class, GlobalExceptionHandler.class},
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
public class PlayerControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private PlayerService playerService;

    @Nested
    class GetPlayers {
        @Test
        void getPlayers_devuelve200() throws Exception {
            when(playerService.getPlayers(anyString(), anyString(), anyString(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(Collections.emptyList()));

            mockMvc.perform(get("/players"))
                    .andExpect(status().isOk());
        }
    }
}
