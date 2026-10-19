package com.furbo.repository.mapping;

import com.furbo.model.mapping.PlayerMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerMappingRepository extends JpaRepository<PlayerMapping, Long> {
    Optional<PlayerMapping> findByWhoScoredPlayerId(Long whoScoredPlayerId);
    Optional<PlayerMapping> findByLocalPlayerId(Long localPlayerId);
}
