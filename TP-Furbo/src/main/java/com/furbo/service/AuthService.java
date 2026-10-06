package com.furbo.service;

import com.furbo.adapter.dto.LoginRequestDTO;
import com.furbo.adapter.dto.RegisterRequestDTO;
import com.furbo.model.Portfolio;
import com.furbo.model.User;
import com.furbo.repository.PortfolioRepository;
import com.furbo.repository.UserRepository;
import com.furbo.security.JwtService;
import com.furbo.security.PasswordHasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordHasher passwordHasher;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Transactional
    public void register(RegisterRequestDTO dto) {
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordHasher.hash(dto.getPassword()));
        userRepository.save(user);

        Portfolio portfolio = new Portfolio();
        portfolio.setUser(user);
        portfolioRepository.save(portfolio);
    }

    @Transactional
    public String login(LoginRequestDTO loginRequest) {
        Optional<User> userOpt = userRepository.findByEmail(loginRequest.getEmail());
        if (userOpt.isPresent() && passwordHasher.verify(loginRequest.getPassword(), userOpt.get().getPassword())) {
            return jwtService.generateToken(userOpt.get().getEmail());
        }
        throw new RuntimeException("Invalid credentials");
    }
}
