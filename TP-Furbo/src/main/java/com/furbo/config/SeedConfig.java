package com.furbo.config;

import com.furbo.model.User;
import com.furbo.repository.UserRepository;
import com.furbo.security.PasswordHasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Configuration
public class SeedConfig {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordHasher passwordHasher;

    @Bean
    @Transactional
    @Order(1)
    CommandLineRunner initDatabase() {
        return args -> {
            if (userRepository.findByEmail("admin@furbo.com").isEmpty()) {
                User admin = new User();
                admin.setEmail("admin@furbo.com");
                admin.setPassword(passwordHasher.hash("admin123"));
                admin.setBalance(BigDecimal.valueOf(100));
                admin.setIsSuperuser(true);
                userRepository.save(admin);
            }
        };
    }
}
