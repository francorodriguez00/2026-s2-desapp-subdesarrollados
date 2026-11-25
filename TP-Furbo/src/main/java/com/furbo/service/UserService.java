package com.furbo.service;

import com.furbo.repository.PortfolioRepository;
import com.furbo.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;

    public UserService(UserRepository userRepository, PortfolioRepository portfolioRepository) {
        this.userRepository = userRepository;
        this.portfolioRepository = portfolioRepository;
    }
    //Queda para futuras acciones de usuario
}
