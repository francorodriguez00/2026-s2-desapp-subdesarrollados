package com.furbo.config;

import com.furbo.model.User;
import com.furbo.repository.UserRepository;
import com.furbo.security.PasswordHasher;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@Order(1)
@Profile("!whoscored-import")
public class SeedConfig implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public SeedConfig(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.findByEmail("admin@furbo.com").isEmpty()) {
            User admin = new User();
            admin.setEmail("admin@furbo.com");
            admin.setPassword(passwordHasher.hash("admin123"));
            admin.setBalance(BigDecimal.valueOf(1000));
            admin.setIsSuperuser(true);
            userRepository.save(admin);
        }
    }
}
