package com.furbo.unit.controller;

import com.furbo.adapter.dto.LoginRequestDTO;
import com.furbo.controller.AuthController;
import com.furbo.service.AuthService;
import com.furbo.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AuthController.class, com.furbo.controller.GlobalExceptionHandler.class})
@Import(com.furbo.config.SecurityConfig.class)
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    @MockBean
    private com.furbo.security.JwtService jwtService;

    @MockBean
    private com.furbo.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void login_ValidCredentials_ReturnsToken() throws Exception {
        when(authService.login(any(LoginRequestDTO.class))).thenReturn("fake-token");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@test.com\", \"password\":\"password\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @org.junit.jupiter.api.Disabled
    void login_InvalidCredentials_ReturnsUnauthorized() throws Exception {
        when(authService.login(any(LoginRequestDTO.class))).thenThrow(new com.furbo.exceptions.InvalidCredentialsException("Invalid"));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@test.com\", \"password\":\"wrong\"}"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isUnauthorized());
    }
}
