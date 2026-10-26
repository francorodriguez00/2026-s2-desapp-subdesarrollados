package com.furbo.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.furbo.config.SecurityConfig;
import com.furbo.controller.OrderController;
import com.furbo.repository.UserRepository;
import com.furbo.service.OrderService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
public class OrderControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private OrderService orderService;
    @MockBean private UserRepository userRepository;
    @MockBean private com.furbo.security.JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean private com.furbo.security.JwtService jwtService;

    @Nested
    class Buy {
        @Test
        @WithMockUser
        void conStockSuficiente_devuelve200() throws Exception {
            when(userRepository.findByEmail("user")).thenReturn(Optional.of(new com.furbo.model.User()));
            
            String json = "{\"playerId\": 1, \"quantity\": 1}";
            mockMvc.perform(post("/orders/buy")
                    .with(user("user"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    class Sell {
        @Test
        @WithMockUser
        void conDatosValidos_devuelve200() throws Exception {
            when(userRepository.findByEmail("user")).thenReturn(Optional.of(new com.furbo.model.User()));
            
            String json = "{\"playerId\": 1, \"quantity\": 1}";
            mockMvc.perform(post("/orders/sell")
                    .with(user("user"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                    .andExpect(status().isOk());
        }
    }
}
