package com.furbo.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.furbo.controller.GlobalExceptionHandler;
import com.furbo.controller.UserController;
import com.furbo.model.Portfolio;
import com.furbo.model.Transaction;
import com.furbo.model.User;
import com.furbo.repository.PortfolioRepository;
import com.furbo.repository.TransactionRepository;
import com.furbo.repository.UserRepository;
import com.furbo.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = {UserController.class, GlobalExceptionHandler.class},
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private PortfolioRepository portfolioRepository;
    @MockBean private TransactionRepository transactionRepository;
    @MockBean private UserRepository userRepository;

    private static final Principal PRINCIPAL = () -> "test@furbo.com";

    @Nested
    class PortfolioTests {
        @Test
        void getPortfolio_ExistingUser_Returns200() throws Exception {
            User user = new User();
            user.setEmail("test@furbo.com");
            when(userRepository.findByEmail("test@furbo.com")).thenReturn(Optional.of(user));
            when(portfolioRepository.findByUser(user)).thenReturn(Optional.of(new Portfolio()));

            mockMvc.perform(get("/users/portfolio")
                    .principal(PRINCIPAL)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        void getPortfolio_NonExistingUser_Returns404() throws Exception {
            when(userRepository.findByEmail("test@furbo.com")).thenReturn(Optional.empty());

            mockMvc.perform(get("/users/portfolio")
                    .principal(PRINCIPAL))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class TransactionTests {
        @Test
        void getTransactions_Returns200() throws Exception {
            User user = new User();
            user.setId(1L);
            user.setEmail("test@furbo.com");
            when(userRepository.findByEmail("test@furbo.com")).thenReturn(Optional.of(user));
            when(transactionRepository.findByBuyerIdOrSellerId(1L, 1L)).thenReturn(List.of(new Transaction()));

            mockMvc.perform(get("/users/transactions")
                    .principal(PRINCIPAL)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }
}
