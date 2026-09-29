package com.furbo.domain.model;

import jakarta.persistence.*;

@Entity
@Table(name = "leagues")
public class League {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private Long externalId;
    
    private String name;
}
