package com.furbo.model.mapping;

import com.furbo.model.Player;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "player_mappings", indexes = {
    @Index(columnList = "whoscored_player_id", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "local_player_id", nullable = false, unique = true)
    private Player localPlayer;

    @Column(name = "whoscored_player_id", nullable = false)
    private Long whoScoredPlayerId;
}
