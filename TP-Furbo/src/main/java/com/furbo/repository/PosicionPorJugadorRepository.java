package com.furbo.repository;

import com.furbo.model.PosicionPorJugador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PosicionPorJugadorRepository extends JpaRepository<PosicionPorJugador, Long> {
    Optional<PosicionPorJugador> findByPortfolioIdAndPlayerId(Long portfolioId, Long playerId);
}
