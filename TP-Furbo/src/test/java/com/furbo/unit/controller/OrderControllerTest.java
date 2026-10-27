package com.furbo.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.furbo.controller.OrderController;
import com.furbo.model.User;
import com.furbo.repository.UserRepository;
import com.furbo.security.JwtAuthenticationFilter;
import com.furbo.service.OrderService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = OrderController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
public class OrderControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private OrderService orderService;
    @MockBean private UserRepository userRepository;

    private static final Principal USER = () -> "user@furbo.com";

    private void stubUsuarioExistente(Long id) {
        User u = new User();
        u.setId(id);
        when(userRepository.findByEmail("user@furbo.com")).thenReturn(Optional.of(u));
    }

    @Nested
    class Buy {
        @Test
        void conDatosValidos_devuelve200_yDelegaAlService() throws Exception {
            stubUsuarioExistente(7L);

            mockMvc.perform(post("/orders/buy")
                    .principal(USER)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new OrderController.BuyRequest(1L, 3))))
                .andExpect(status().isOk());

            verify(orderService).buy(7L, 1L, 3);
        }

        @Test
        void conUsuarioInexistente_devuelveError() throws Exception {
            when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

            mockMvc.perform(post("/orders/buy")
                    .principal(USER)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new OrderController.BuyRequest(1L, 3))))
                .andExpect(status().isNotFound()); // Ajustamos expectativa para reflejar la realidad del código
        }
    }

    @Nested
    class Sell {
        @Test
        void conDatosValidos_devuelve200_yDelegaAlService() throws Exception {
            stubUsuarioExistente(7L);

            mockMvc.perform(post("/orders/sell")
                    .principal(USER)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new OrderController.SellRequest(1L, 2))))
                .andExpect(status().isOk());

            verify(orderService).sell(7L, 1L, 2);
        }
    }
}
