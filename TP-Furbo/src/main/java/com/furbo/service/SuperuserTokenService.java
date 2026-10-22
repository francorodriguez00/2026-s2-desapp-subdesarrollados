package com.furbo.service;

import com.furbo.model.*;
import com.furbo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SuperuserTokenService {

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final PosicionPorJugadorRepository posicionRepository;
    private final PlayerRepository playerRepository;

    public SuperuserTokenService(UserRepository userRepository, PortfolioRepository portfolioRepository,
                                 PosicionPorJugadorRepository posicionRepository, PlayerRepository playerRepository) {
        this.userRepository = userRepository;
        this.portfolioRepository = portfolioRepository;
        this.posicionRepository = posicionRepository;
        this.playerRepository = playerRepository;
    }

    @Transactional
    public Portfolio asegurarPortfolioSuperuser() {
        User superuser = userRepository.findByIsSuperuserTrue()
                .orElseThrow(() -> new IllegalStateException("No existe superusuario"));
        
        return portfolioRepository.findByUser(superuser)
                .orElseGet(() -> {
                    Portfolio p = new Portfolio();
                    p.setUser(superuser);
                    return portfolioRepository.save(p);
                });
    }

    @Transactional
    public void asignarTokens(Player jugador) {
        Portfolio portfolio = asegurarPortfolioSuperuser();
        if (posicionRepository.findByPortfolioIdAndPlayerId(portfolio.getId(), jugador.getExternalId()).isEmpty()) {
            PosicionPorJugador posicion = new PosicionPorJugador();
            posicion.setPortfolio(portfolio);
            posicion.setPlayerId(jugador.getExternalId());
            posicion.setCantidad(100);
            posicionRepository.save(posicion);
        }
    }

    @Transactional
    public void sincronizarTodos() {
        Portfolio portfolio = asegurarPortfolioSuperuser();
        List<Player> todosLosJugadores = playerRepository.findAll();
        
        Set<Long> jugadorIdsConPosicion = posicionRepository.findAll().stream()
                .filter(p -> p.getPortfolio().getId().equals(portfolio.getId()))
                .map(PosicionPorJugador::getPlayerId)
                .collect(Collectors.toSet());

        List<PosicionPorJugador> nuevas = todosLosJugadores.stream()
                .filter(j -> !jugadorIdsConPosicion.contains(j.getExternalId()))
                .map(j -> {
                    PosicionPorJugador p = new PosicionPorJugador();
                    p.setPortfolio(portfolio);
                    p.setPlayerId(j.getExternalId());
                    p.setCantidad(100);
                    return p;
                })
                .collect(Collectors.toList());

        if (!nuevas.isEmpty()) {
            posicionRepository.saveAll(nuevas);
        }
    }
}
