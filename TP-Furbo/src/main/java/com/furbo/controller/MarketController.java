package com.furbo.controller;

import com.furbo.service.PriceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/market")
public class MarketController {

    @Autowired
    private PriceProvider priceProvider;

    @GetMapping("/price/{playerId}")
    public BigDecimal getPlayerPrice(@PathVariable Long playerId) {
        return priceProvider.getCurrentPrice(playerId);
    }
}
