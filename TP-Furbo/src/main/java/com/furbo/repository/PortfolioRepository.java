package com.furbo.repository;

import com.furbo.model.Portfolio;
import com.furbo.model.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    Optional<Portfolio> findByUser(User user);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Portfolio p WHERE p.user = :user")
    Optional<Portfolio> findByUserWithLock(@Param("user") User user);
}
