package com.furbo.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;

@Entity
@Table(name = "strategy_config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String strategyCode;
    private Integer version;
    
    @JdbcTypeCode(SqlTypes.JSON)
    private StrategyConfigData configJson;
    
    private Boolean active;
    private LocalDateTime createdAt;
}
