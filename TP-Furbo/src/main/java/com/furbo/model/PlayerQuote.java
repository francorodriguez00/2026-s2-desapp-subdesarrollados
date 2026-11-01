package com.furbo.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "player_quote", indexes = @Index(columnList = "playerId, calculatedAt DESC, id DESC"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerQuote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long playerId;
    private String strategyCode;
    private Integer strategyVersion;
    private Long strategyConfigId;
    
    @Column(precision = 6, scale = 4)
    private BigDecimal score;
    
    @Column(precision = 12, scale = 2)
    private BigDecimal price;
    
    private LocalDateTime statsUpdatedAt;
    private LocalDateTime calculatedAt;
    
    @Enumerated(EnumType.STRING)
    private QuoteTrigger trigger;
}
