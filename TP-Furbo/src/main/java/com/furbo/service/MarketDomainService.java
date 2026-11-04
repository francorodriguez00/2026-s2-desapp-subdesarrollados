package com.furbo.service;

import com.furbo.model.SellOffer;
import com.furbo.model.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class MarketDomainService {

    public boolean canUserAfford(User buyer, int quantity, BigDecimal unitPrice) {
        BigDecimal totalCost = unitPrice.multiply(BigDecimal.valueOf(quantity));
        return buyer.getBalance().compareTo(totalCost) >= 0;
    }

    public void validateSellQuantity(int currentQuantity, int reservedQuantity, int requestedQuantity) {
        if (currentQuantity - reservedQuantity < requestedQuantity) {
            throw new IllegalStateException("Stock insuficiente disponible para vender");
        }
    }
}
