package com.furbo.repository;

import com.furbo.model.PlayerSourceId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PlayerSourceIdRepository extends JpaRepository<PlayerSourceId, Long> {
    Optional<PlayerSourceId> findBySourceAndExternalId(String source, String externalId);
    Optional<PlayerSourceId> findByPlayerIdAndSource(Long playerId, String source);
    List<PlayerSourceId> findAllBySource(String source);
}
