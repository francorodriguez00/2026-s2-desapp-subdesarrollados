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
    @Autowired private PosicionPorJugadorRepository posicionRepository;
    @Autowired private PortfolioRepository portfolioRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private PriceProvider priceProvider;
    @Autowired private MarketDomainService domainService;

    @Transactional
    public void sell(Long sellerId, Long playerId, int quantity) {
        User seller = userRepository.findById(sellerId).orElseThrow();
        Portfolio portfolio = portfolioRepository.findByUser(seller)
                .orElseThrow(() -> new IllegalArgumentException("Usuario sin portfolio"));

        PosicionPorJugador posicion = posicionRepository.findByPortfolioIdAndPlayerId(portfolio.getId(), playerId)
                .orElseThrow(() -> new IllegalArgumentException("No posee tenencia de este jugador"));

        domainService.validateSellQuantity(posicion.getCantidad(), posicion.getReservedQuantity(), quantity);

        posicion.setReservedQuantity(posicion.getReservedQuantity() + quantity);
        posicionRepository.save(posicion);

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
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        BigDecimal price = priceProvider.getCurrentPrice(playerId);
        User buyer = userRepository.findById(buyerId).orElseThrow();
        Portfolio buyerPortfolio = portfolioRepository.findByUser(buyer)
                .orElseGet(() -> {
                    Portfolio p = new Portfolio();
                    p.setUser(buyer);
                    return portfolioRepository.save(p);
                });

        if (!domainService.canUserAfford(buyer, quantity, price)) {
            throw new IllegalStateException("Saldo insuficiente");
        }

        buyer.setBalance(buyer.getBalance().subtract(price.multiply(BigDecimal.valueOf(quantity))));
        userRepository.save(buyer);

        List<SellOffer> offers = sellOfferRepository.findOpenOffersForPlayer(playerId);
        String operationId = java.util.UUID.randomUUID().toString();
        int processedQuantity = 0;

        for (SellOffer offer : offers) {
            if (offer.getSellerId().equals(buyerId)) continue; 

            int canTake = Math.min(quantity - processedQuantity, offer.getQuantityRemaining());
            if (canTake <= 0) continue;
            
            // Procesar vendedor (decremento)
            User seller = userRepository.findById(offer.getSellerId()).orElseThrow();
            Portfolio sellerPortfolio = portfolioRepository.findByUser(seller).orElseThrow();
            PosicionPorJugador sellerPos = posicionRepository.findByPortfolioIdAndPlayerId(sellerPortfolio.getId(), playerId).orElseThrow();
            
            sellerPos.setCantidad(sellerPos.getCantidad() - canTake);
            sellerPos.setReservedQuantity(sellerPos.getReservedQuantity() - canTake);
            posicionRepository.save(sellerPos);

            // Procesar comprador (incremento)
            PosicionPorJugador buyerPos = posicionRepository.findByPortfolioIdAndPlayerId(buyerPortfolio.getId(), playerId)
                    .orElseGet(() -> {
                        PosicionPorJugador p = new PosicionPorJugador();
                        p.setPortfolio(buyerPortfolio);
                        p.setPlayerId(playerId);
                        return p;
                    });
            buyerPos.setCantidad(buyerPos.getCantidad() + canTake);
            posicionRepository.save(buyerPos);

            offer.setQuantityRemaining(offer.getQuantityRemaining() - canTake);
            if (offer.getQuantityRemaining() == 0) offer.setStatus(SellOffer.OfferStatus.FILLED);
            sellOfferRepository.save(offer);

            // Registro de transacción individual
            Transaction transaction = new Transaction();
            transaction.setBuyerId(buyerId);
            transaction.setSellerId(offer.getSellerId());
            transaction.setPlayerId(playerId);
            transaction.setQuantity(canTake);
            transaction.setUnitPrice(price);
            transaction.setOperationId(operationId);
            transaction.setCreatedAt(java.time.LocalDateTime.now());
            transactionRepository.save(transaction);

            processedQuantity += canTake;
            if (processedQuantity == quantity) break;
        }

        if (processedQuantity < quantity) throw new IllegalStateException("Stock insuficiente");
    }
}
