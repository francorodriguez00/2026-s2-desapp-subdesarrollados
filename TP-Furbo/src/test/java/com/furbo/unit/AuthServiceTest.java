package com.furbo.unit;

import com.furbo.adapter.dto.LoginRequestDTO;
import com.furbo.model.User;
import com.furbo.repository.UserRepository;
import com.furbo.security.JwtService;
import com.furbo.security.PasswordHasher;
import com.furbo.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordHasher passwordHasher;

    @InjectMocks
    private AuthService authService;

    private LoginRequestDTO loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("test@furbo.com");
        loginRequest.setPassword("correctPassword");

        user = new User();
        user.setEmail("test@furbo.com");
        user.setPassword("hashedPassword");
    }

    @Test
    void shouldReturnTokenWhenCredentialsAreValid() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordHasher.verify(loginRequest.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtService.generateToken(user.getEmail())).thenReturn("dummyToken");

        String token = authService.login(loginRequest);

        assertEquals("dummyToken", token);
        verify(jwtService).generateToken(user.getEmail());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsInvalid() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordHasher.verify(loginRequest.getPassword(), user.getPassword())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
        verifyNoInteractions(jwtService);
    }
}
