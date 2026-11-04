package com.furbo.repository;

import com.furbo.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findByPlayerIdOrderByCreatedAtDesc(Long playerId);

    Optional<Trade> findTopByPlayerIdOrderByCreatedAtDesc(Long playerId);
}
