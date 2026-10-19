package com.furbo.service;

import com.furbo.model.*;
import com.furbo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    @Autowired private SellOfferRepository sellOfferRepository;
    @Autowired private PortfolioRepository portfolioRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PriceProvider priceProvider;
    @Autowired private MarketDomainService domainService;

    @Transactional
    public void sell(Long sellerId, Long playerId, int quantity) {
        Portfolio portfolio = portfolioRepository.findByUserAndPlayerId(sellerId, playerId)
                .orElseThrow(() -> new IllegalArgumentException("No posee tenencia de este jugador"));

        domainService.validateSellQuantity(portfolio.getQuantity(), portfolio.getReservedQuantity(), quantity);

        portfolio.setReservedQuantity(portfolio.getReservedQuantity() + quantity);
        portfolioRepository.save(portfolio);

        SellOffer offer = new SellOffer();
        offer.setSellerId(sellerId);
        offer.setPlayerId(playerId);
        offer.setQuantityTotal(quantity);
        offer.setQuantityRemaining(quantity);
        offer.setStatus(SellOffer.OfferStatus.OPEN);
        sellOfferRepository.save(offer);
    }

    @Transactional
    public void buy(Long buyerId, Long playerId, int quantity) {
        BigDecimal price = priceProvider.getCurrentPrice(playerId);
        User buyer = userRepository.findById(buyerId).orElseThrow();

        if (!domainService.canUserAfford(buyer, quantity, price)) {
            throw new IllegalStateException("Saldo insuficiente");
        }

        List<SellOffer> offers = sellOfferRepository.findOpenOffersForPlayer(playerId);
        int remainingToBuy = quantity;

        for (SellOffer offer : offers) {
            if (offer.getSellerId().equals(buyerId)) continue; // No comprarse a sí mismo

            int canTake = Math.min(remainingToBuy, offer.getQuantityRemaining());
            
            // Lógica de transferencia simplificada para el prototipo
            offer.setQuantityRemaining(offer.getQuantityRemaining() - canTake);
            if (offer.getQuantityRemaining() == 0) offer.setStatus(SellOffer.OfferStatus.FILLED);
            sellOfferRepository.save(offer);

            remainingToBuy -= canTake;
            if (remainingToBuy == 0) break;
        }

        if (remainingToBuy > 0) throw new IllegalStateException("Stock insuficiente");

        buyer.setBalance(buyer.getBalance().subtract(price.multiply(BigDecimal.valueOf(quantity))));
        userRepository.save(buyer);
    }
}
