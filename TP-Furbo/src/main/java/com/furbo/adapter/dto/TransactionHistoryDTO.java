package com.furbo.adapter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionHistoryDTO {
    private String operationId;
    private String playerName;
    private Integer totalQuantity;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private String type; // "COMPRA" o "VENTA"
    private List<TransactionDetailDTO> details;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TransactionDetailDTO {
        private Integer quantity;
        private BigDecimal unitPrice;
    }
}
