package com.furbo.controller;

import com.furbo.service.PriceProvider;
import com.furbo.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

import com.furbo.adapter.dto.SellOfferDTO;
import com.furbo.model.Player;
import com.furbo.model.SellOffer;
import com.furbo.repository.PlayerRepository;
import com.furbo.repository.PosicionPorJugadorRepository;
import com.furbo.repository.SellOfferRepository;
import com.furbo.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/market")
public class MarketController {

    @Autowired private PriceProvider priceProvider;
    @Autowired private OrderService orderService;
    @Autowired private SellOfferRepository sellOfferRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PlayerRepository playerRepository;
    @Autowired private PosicionPorJugadorRepository posicionRepository;
    @Autowired private com.furbo.repository.PortfolioRepository portfolioRepository;

    @GetMapping("/price/{playerId}")
    public BigDecimal getPlayerPrice(@PathVariable Long playerId) {
        return priceProvider.getCurrentPrice(playerId);
    }

    @GetMapping("/book/{playerId}")
    public ResponseEntity<?> getBook(@PathVariable Long playerId) {
        return ResponseEntity.ok(orderService.getOrderBook(playerId));
    }

    @GetMapping("/last-trade/{playerId}")
    public ResponseEntity<?> getLastTrade(@PathVariable Long playerId) {
        BigDecimal lastTrade = orderService.getLastTrade(playerId);
        if (lastTrade == null) {
            lastTrade = priceProvider.getCurrentPrice(playerId);
        }
        return ResponseEntity.ok(lastTrade);
    }

    @GetMapping("/my-offers")
    public List<SellOfferDTO> getMyOffers(Principal principal) {
        Long userId = userRepository.findByEmail(principal.getName()).orElseThrow().getId();
        return sellOfferRepository.findAll().stream()
                .filter(so -> so.getSellerId().equals(userId) && so.getStatus() == SellOffer.OfferStatus.OPEN)
                .map(so -> new SellOfferDTO(
                        so.getId(),
                        playerRepository.findById(so.getPlayerId()).map(Player::getName).orElse("Desconocido"),
                        so.getQuantityRemaining(),
                        priceProvider.getCurrentPrice(so.getPlayerId()),
                        so.getCreatedAt()
                )).collect(Collectors.toList());
    }

    @DeleteMapping("/offers/{offerId}")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> cancelOffer(@PathVariable Long offerId, Principal principal) {
        Long userId = userRepository.findByEmail(principal.getName()).orElseThrow().getId();
        SellOffer offer = sellOfferRepository.findById(offerId).orElseThrow();
        
        if (!offer.getSellerId().equals(userId)) return ResponseEntity.status(403).build();

        com.furbo.model.User seller = userRepository.findById(userId).orElseThrow();
        com.furbo.model.Portfolio portfolio = portfolioRepository.findByUser(seller).orElseThrow();
        com.furbo.model.PosicionPorJugador pos = posicionRepository.findByPortfolioIdAndPlayerId(portfolio.getId(), offer.getPlayerId()).orElseThrow();
        
        pos.setReservedQuantity(pos.getReservedQuantity() - offer.getQuantityRemaining());
        posicionRepository.save(pos);
        
        sellOfferRepository.delete(offer);
        return ResponseEntity.ok().build();
    }
}
