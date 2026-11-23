package com.furbo.repository;

import com.furbo.model.SellOffer;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SellOfferRepository extends JpaRepository<SellOffer, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SellOffer s WHERE s.playerId = :playerId AND s.status = 'OPEN' ORDER BY s.createdAt ASC")
    List<SellOffer> findOpenOffersForPlayer(@Param("playerId") Long playerId);
}
