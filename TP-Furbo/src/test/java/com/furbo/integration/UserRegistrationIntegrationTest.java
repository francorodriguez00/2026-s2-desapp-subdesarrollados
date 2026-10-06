package com.furbo.integration;

import com.furbo.adapter.dto.RegisterRequestDTO;
import com.furbo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
public class UserRegistrationIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldRegisterUserSuccessfully() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setEmail("test@furbo.com");
        dto.setPassword("password123");

        ResponseEntity<Void> response = restTemplate.postForEntity("/auth/register", dto, Void.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(userRepository.findByEmail("test@furbo.com")).isPresent();
    }
}