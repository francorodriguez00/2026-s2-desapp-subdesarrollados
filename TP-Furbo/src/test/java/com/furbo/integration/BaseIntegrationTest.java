package com.furbo.integration;

import com.furbo.adapter.dto.LoginRequestDTO;
import com.furbo.adapter.dto.RegisterRequestDTO;
import com.furbo.config.TestcontainersConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfig.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public abstract class BaseIntegrationTest {

    protected static final String PASSWORD = "password123";

    @MockBean protected RestTemplate restTemplate; 
    @Autowired protected TestRestTemplate rest;

    protected String registrarUsuario() {
        String email = "user-" + UUID.randomUUID() + "@furbo.com";
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setEmail(email);
        dto.setPassword(PASSWORD);
        assertThat(rest.postForEntity("/auth/register", dto, Void.class)
                .getStatusCode().is2xxSuccessful()).isTrue();
        return email;
    }

    protected String login(String email, String password) {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail(email);
        dto.setPassword(password);
        ResponseEntity<Map> res = rest.postForEntity("/auth/login", dto, Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        return (String) res.getBody().get("token");
    }

    protected String login(String email) { return login(email, PASSWORD); }

    protected HttpHeaders bearer(String token) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        return h;
    }
}
