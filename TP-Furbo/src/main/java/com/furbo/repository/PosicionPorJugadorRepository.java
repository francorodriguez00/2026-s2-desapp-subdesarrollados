package com.furbo.repository;

import com.furbo.model.PosicionPorJugador;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PosicionPorJugadorRepository extends JpaRepository<PosicionPorJugador, Long> {
    Optional<PosicionPorJugador> findByPortfolioIdAndPlayerId(Long portfolioId, Long playerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pos FROM PosicionPorJugador pos WHERE pos.portfolio.id = :portfolioId AND pos.playerId = :playerId")
    Optional<PosicionPorJugador> findByPortfolioIdAndPlayerIdWithLock(@Param("portfolioId") Long portfolioId, @Param("playerId") Long playerId);
}
