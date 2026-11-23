package com.furbo.adapter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioResponseDTO {
    private List<PositionDTO> posiciones;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PositionDTO {
        private Long playerId;
        private Integer cantidad;
    }
}
