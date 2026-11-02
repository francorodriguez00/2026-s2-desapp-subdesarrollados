package com.furbo.repository;

import com.furbo.adapter.dto.PlayerCatalogDTO;
import com.furbo.model.Player;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long>, JpaSpecificationExecutor<Player> {
    List<Player> findByExternalIdIn(Collection<Long> externalIds);

    @Query(value = "SELECT new com.furbo.adapter.dto.PlayerCatalogDTO(p.id, p.name, p.position, p.team, p.league, " +
                   "(SELECT q.price FROM PlayerQuote q WHERE q.playerId = p.id ORDER BY q.calculatedAt DESC, q.id DESC LIMIT 1)) " +
                   "FROM Player p WHERE " +
                   "(:league = '' OR p.league = :league) AND " +
                   "(:position = '' OR p.position = :position) AND " +
                   "(:name = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
                   "ORDER BY (SELECT q.price FROM PlayerQuote q WHERE q.playerId = p.id ORDER BY q.calculatedAt DESC, q.id DESC LIMIT 1) DESC NULLS LAST, p.id ASC",
           countQuery = "SELECT COUNT(p) FROM Player p WHERE " +
                        "(:league = '' OR p.league = :league) AND " +
                        "(:position = '' OR p.position = :position) AND " +
                        "(:name = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<PlayerCatalogDTO> findPlayersCatalog(@Param("league") String league, 
                                             @Param("position") String position, 
                                             @Param("name") String name, 
                                             Pageable pageable);
}
