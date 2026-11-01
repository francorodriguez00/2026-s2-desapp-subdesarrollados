package com.furbo.repository;

import com.furbo.model.StrategyConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StrategyConfigRepository extends JpaRepository<StrategyConfig, Long> {
    Optional<StrategyConfig> findByActiveTrue();
    Optional<StrategyConfig> findTopByStrategyCodeOrderByVersionDesc(String strategyCode);
    Optional<StrategyConfig> findByStrategyCodeAndVersion(String strategyCode, Integer version);
}
