package com.furbo.integration;

import com.furbo.model.Player;
import com.furbo.model.Portfolio;
import com.furbo.model.PosicionPorJugador;
import com.furbo.model.User;
import com.furbo.repository.*;
import com.furbo.service.SuperuserTokenService;
import com.furbo.controller.OrderController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;

class MarketFlowIntegrationTest extends BaseIntegrationTest {

    @Autowired UserRepository userRepository;
    @Autowired PlayerRepository playerRepository;
    @Autowired PortfolioRepository portfolioRepository;
    @Autowired PosicionPorJugadorRepository posicionRepository;
    @Autowired SuperuserTokenService tokenService;

    @Test
    void alAsignarTokens_elSuperuserTiene100_yEsIdempotente() {
        Player p = crearJugador();
        assertThat(cantidadDe("admin@furbo.com", p)).isEqualTo(100);

        tokenService.asignarTokens(p);
        tokenService.sincronizarTodos();

        assertThat(cantidadDe("admin@furbo.com", p)).isEqualTo(100);
    }

    @Test
    void comprarAlSuperuser_descuentaSaldo_yMueveLasPosiciones() {
        Player p = crearJugador();
        String email = usuarioConSaldo("1000000");

        var res = comprar(login(email), p, 3);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cantidadDe(email, p)).isEqualTo(3);
        assertThat(cantidadDe("admin@furbo.com", p)).isEqualTo(97);
        assertThat(saldoDe(email)).isLessThan(new BigDecimal("1000000"));
    }

    @Test
    void comprarMasDeLoDisponible_haceRollbackCompleto() {
        Player p = crearJugador();
        String email = usuarioConSaldo("1000000"); 

        var res = comprar(login(email), p, 101);

        assertThat(res.getStatusCode().is4xxClientError()).isTrue();
        assertThat(cantidadDe("admin@furbo.com", p)).isEqualTo(100);
        assertThat(cantidadDe(email, p)).isZero();
        assertThat(saldoDe(email)).isEqualByComparingTo("1000000");
    }

    @Test
    void comprarCantidadNegativa_esRechazada_yNoCambiaNada() {
        Player p = crearJugador();
        String email = usuarioConSaldo("1000");

        var res = comprar(login(email), p, -5);

        assertThat(res.getStatusCode().is4xxClientError()).isTrue();
        assertThat(saldoDe(email)).isEqualByComparingTo("1000");
        assertThat(cantidadDe("admin@furbo.com", p)).isEqualTo(100);
    }

    @Test
    void vender_reservaLosTokens() {
        Player p = crearJugador();
        String email = usuarioConSaldo("1000000");
        String token = login(email);
        comprar(token, p, 5);

        var res = rest.exchange("/orders/sell", HttpMethod.POST,
                new HttpEntity<>(new OrderController.SellRequest(p.getId(), 2), bearer(token)), Void.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(reservadosDe(email, p)).isEqualTo(2);
    }

    private ResponseEntity<Void> comprar(String token, Player p, int qty) {
        return rest.exchange("/orders/buy", HttpMethod.POST,
                new HttpEntity<>(new OrderController.BuyRequest(p.getId(), qty), bearer(token)), Void.class);
    }

    private Player crearJugador() {
        Player p = playerRepository.save(Player.builder()
                .externalId(ThreadLocalRandom.current().nextLong(1_000_000L, Long.MAX_VALUE))
                .name("Jugador " + UUID.randomUUID())
                .build());
        tokenService.asignarTokens(p);
        return p;
    }

    private String usuarioConSaldo(String saldo) {
        String email = registrarUsuario();
        User u = userRepository.findByEmail(email).orElseThrow();
        u.setBalance(new BigDecimal(saldo));
        userRepository.save(u);
        return email;
    }

    private Optional<PosicionPorJugador> posicion(String email, Player p) {
        User u = userRepository.findByEmail(email).orElseThrow();
        Portfolio pf = portfolioRepository.findByUser(u).orElseThrow();
        return posicionRepository.findByPortfolioIdAndPlayerId(pf.getId(), p.getId());
    }

    private int cantidadDe(String email, Player p) {
        return posicion(email, p).map(PosicionPorJugador::getCantidad).orElse(0);
    }

    private int reservadosDe(String email, Player p) {
        return posicion(email, p).map(PosicionPorJugador::getReservedQuantity).orElse(0);
    }

    private BigDecimal saldoDe(String email) {
        return userRepository.findByEmail(email).orElseThrow().getBalance();
    }
}
