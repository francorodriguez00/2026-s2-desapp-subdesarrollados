package com.furbo.unit.controller;

import com.furbo.config.SecurityConfig;
import com.furbo.controller.UserController;
import com.furbo.repository.PortfolioRepository;
import com.furbo.repository.TransactionRepository;
import com.furbo.repository.UserRepository;
import com.furbo.security.JwtAuthenticationFilter;
import com.furbo.security.JwtService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
public class UserControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private PortfolioRepository portfolioRepository;
    @MockBean private TransactionRepository transactionRepository;
    @MockBean private UserRepository userRepository;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean private JwtService jwtService;

    @Nested
    class Portfolio {
        @Test
        @WithMockUser
        void getPortfolio_Returns200() throws Exception {
            when(userRepository.findByEmail("user")).thenReturn(Optional.of(new com.furbo.model.User()));
            
            mockMvc.perform(get("/users/portfolio").with(user("user")))
                    .andExpect(status().isOk());
        }
    }
}
