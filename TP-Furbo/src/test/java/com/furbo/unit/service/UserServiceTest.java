package com.furbo.unit.service;

import com.furbo.repository.PortfolioRepository;
import com.furbo.repository.UserRepository;
import com.furbo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PortfolioRepository portfolioRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository, portfolioRepository);
    }

    @Test
    void testPlaceholder() {
        // Clase creada para coverage.
    }
}
