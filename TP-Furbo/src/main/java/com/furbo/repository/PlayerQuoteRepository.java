package com.furbo.repository;

import com.furbo.model.PlayerQuote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PlayerQuoteRepository extends JpaRepository<PlayerQuote, Long> {
    Optional<PlayerQuote> findFirstByPlayerIdOrderByCalculatedAtDescIdDesc(Long playerId);
    Page<PlayerQuote> findByPlayerIdOrderByCalculatedAtDescIdDesc(Long playerId, Pageable pageable);
}
