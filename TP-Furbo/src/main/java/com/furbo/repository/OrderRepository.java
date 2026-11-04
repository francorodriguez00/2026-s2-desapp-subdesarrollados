package com.furbo.repository;

import com.furbo.model.Order;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdWithLock(@Param("id") Long id);

    @Query("SELECT o FROM Order o WHERE o.playerId = :playerId AND o.status IN ('OPEN', 'PARTIAL') AND o.side = 'SELL' ORDER BY o.price ASC, o.createdAt ASC")
    List<Order> findOpenSellOrdersForPlayer(@Param("playerId") Long playerId);

    @Query("SELECT o FROM Order o WHERE o.playerId = :playerId AND o.status IN ('OPEN', 'PARTIAL') AND o.side = 'BUY' ORDER BY o.price DESC, o.createdAt ASC")
    List<Order> findOpenBuyOrdersForPlayer(@Param("playerId") Long playerId);

    List<Order> findByUserIdAndStatusIn(Long userId, List<Order.OrderStatus> statuses);

    List<Order> findByPlayerIdAndStatusIn(Long playerId, List<Order.OrderStatus> statuses);
}
