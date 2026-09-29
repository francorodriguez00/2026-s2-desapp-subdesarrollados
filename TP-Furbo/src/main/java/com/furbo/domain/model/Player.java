package com.furbo.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "players")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private Long externalId;
    
    private String name;
    
    @ManyToOne
    private Team team;
    
    @ManyToOne
    private League league;
    
    private String position;
    private String nationality;
    
    @Column(columnDefinition = "jsonb")
    private String metrics; // Future WhoScored integration
}
