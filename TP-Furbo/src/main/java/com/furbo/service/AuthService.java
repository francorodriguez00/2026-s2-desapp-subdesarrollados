package com.furbo.service;

import com.furbo.adapter.dto.LoginRequestDTO;
import com.furbo.model.User;
import com.furbo.repository.UserRepository;
import com.furbo.security.JwtService;
import com.furbo.security.PasswordHasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordHasher passwordHasher;

    public String login(LoginRequestDTO loginRequest) {
        Optional<User> userOpt = userRepository.findByEmail(loginRequest.getEmail());
        if (userOpt.isPresent() && passwordHasher.verify(loginRequest.getPassword(), userOpt.get().getPassword())) {
            return jwtService.generateToken(userOpt.get().getEmail());
        }
        throw new RuntimeException("Invalid credentials");
    }
}
