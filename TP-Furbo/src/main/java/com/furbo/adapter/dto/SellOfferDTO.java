package com.furbo.adapter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SellOfferDTO {
    private Long id;
    private String playerName;
    private Integer quantityRemaining;
    private BigDecimal currentPrice;
    private LocalDateTime createdAt;
}
