package com.furbo.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class SecurityIntegrationTest extends BaseIntegrationTest {

    @Test
    void apiDocs_esPublico() {
        assertThat(rest.getForEntity("/v3/api-docs", String.class).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void endpointProtegido_sinToken_esRechazado() {
        assertThat(rest.getForEntity("/users/portfolio", String.class).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void endpointProtegido_conTokenInvalido_esRechazado() {
        var res = rest.exchange("/users/portfolio", HttpMethod.GET,
                new HttpEntity<>(bearer("token.basura.xyz")), String.class);
        assertThat(res.getStatusCode()).isIn(HttpStatus.FORBIDDEN, HttpStatus.UNAUTHORIZED);
    }

    @Test
    void registro_login_yAccesoConToken() {
        String token = login(registrarUsuario());

        var res = rest.exchange("/users/portfolio", HttpMethod.GET,
                new HttpEntity<>(bearer(token)), String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).doesNotContain("password");
    }

    @Test
    void actuatorHealth_esPublico() {
        assertThat(rest.getForEntity("/actuator/health", String.class).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void actuatorInfo_esPublico() {
        ResponseEntity<String> res = rest.getForEntity("/actuator/info", String.class);
        assertThat(res.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND);
    }

    @Test
    void actuatorPrometheus_sinToken_esRechazado() {
        assertThat(rest.getForEntity("/actuator/prometheus", String.class).getStatusCode())
                .isIn(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
    }

    @Test
    void actuatorPrometheus_conToken_esExitoso() {
        String token = login(registrarUsuario());
        var res = rest.exchange("/actuator/prometheus", HttpMethod.GET,
                new HttpEntity<>(bearer(token)), String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).contains("jvm_memory_used_bytes");
    }
}
