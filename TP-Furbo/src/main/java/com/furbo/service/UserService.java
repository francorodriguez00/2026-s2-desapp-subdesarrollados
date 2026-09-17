package com.furbo.service;

import com.furbo.adapter.dto.UserRegistrationDTO;
import com.furbo.model.Portfolio;
import com.furbo.model.User;
import com.furbo.repository.PortfolioRepository;
import com.furbo.repository.UserRepository;
import com.furbo.security.PasswordHasher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final PasswordHasher passwordHasher;

    public UserService(UserRepository userRepository, PortfolioRepository portfolioRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.portfolioRepository = portfolioRepository;
        this.passwordHasher = passwordHasher;
    }

    @Transactional
    public void register(UserRegistrationDTO dto) {
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordHasher.hash(dto.getPassword()));
        userRepository.save(user);

        Portfolio portfolio = new Portfolio();
        portfolio.setUser(user);
        portfolioRepository.save(portfolio);
    }
}
