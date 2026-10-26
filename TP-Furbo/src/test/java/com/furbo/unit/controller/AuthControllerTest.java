package com.furbo.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.furbo.adapter.dto.LoginRequestDTO;
import com.furbo.adapter.dto.RegisterRequestDTO;
import com.furbo.config.SecurityConfig;
import com.furbo.controller.AuthController;
import com.furbo.controller.GlobalExceptionHandler;
import com.furbo.exceptions.InvalidCredentialsException;
import com.furbo.security.JwtAuthenticationFilter;
import com.furbo.security.JwtService;
import com.furbo.service.AuthService;
import com.furbo.service.UserService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AuthService authService;
    @MockBean private UserService userService;
    @MockBean private JwtService jwtService;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean private com.furbo.controller.GlobalExceptionHandler globalExceptionHandler;

    @Nested
    class Register {
        @Test
        void conDatosValidos_devuelve200() throws Exception {
            RegisterRequestDTO dto = new RegisterRequestDTO();
            dto.setEmail("test@furbo.com");
            dto.setPassword("pass");

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    class Login {
        @Test
        void conCredencialesValidas_devuelve200() throws Exception {
            LoginRequestDTO dto = new LoginRequestDTO();
            dto.setEmail("test@furbo.com");
            dto.setPassword("pass");

            when(authService.login(any())).thenReturn("token-123");

            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk());
        }

        @Test
        void conCredencialesInvalidas_devuelve401() throws Exception {
            LoginRequestDTO dto = new LoginRequestDTO();
            dto.setEmail("test@furbo.com");
            dto.setPassword("wrong");

            when(authService.login(any())).thenThrow(new InvalidCredentialsException("Invalid"));

            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                    .andDo(result -> System.out.println("RESPONSE STATUS: " + result.getResponse().getStatus()))
                    .andExpect(status().isUnauthorized());
        }
    }
}
