package com.furbo.controller;

import com.furbo.adapter.dto.PortfolioResponseDTO;
import com.furbo.model.Player;
import com.furbo.model.Portfolio;
import com.furbo.model.Transaction;
import com.furbo.repository.PortfolioRepository;
import com.furbo.repository.PlayerRepository;
import com.furbo.repository.TransactionRepository;
import com.furbo.repository.UserRepository;
import com.furbo.service.PriceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired private PortfolioRepository portfolioRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PlayerRepository playerRepository;
    @Autowired private PriceProvider priceProvider;

    @GetMapping("/portfolio")
    public PortfolioResponseDTO getPortfolio(Principal principal) {
        com.furbo.model.User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        Portfolio portfolio = portfolioRepository.findByUser(user).orElseGet(() -> {
            Portfolio p = new Portfolio();
            p.setUser(user);
            return portfolioRepository.save(p);
        });

        BigDecimal totalInvertido = BigDecimal.ZERO;
        List<PortfolioResponseDTO.PositionDTO> posDTOs = portfolio.getPosiciones().stream()
                .map(p -> {
                    Player player = playerRepository.findById(p.getPlayerId()).orElse(new Player());
                    BigDecimal price = priceProvider.getCurrentPrice(p.getPlayerId());
                    BigDecimal total = price.multiply(BigDecimal.valueOf(p.getCantidad()));
                    return new PortfolioResponseDTO.PositionDTO(p.getPlayerId(), player.getName(), p.getCantidad(), price, total);
                })
                .collect(Collectors.toList());

        for (PortfolioResponseDTO.PositionDTO pos : posDTOs) {
            totalInvertido = totalInvertido.add(pos.getValorTotal());
        }

        return new PortfolioResponseDTO(posDTOs, user.getBalance(), totalInvertido);
    }

    @GetMapping("/balance")
    public BigDecimal getBalance(Principal principal) {
        return userRepository.findByEmail(principal.getName()).orElseThrow().getBalance();
    }

    @GetMapping("/transactions")
    public List<Transaction> getTransactions(Principal principal) {
        Long userId = userRepository.findByEmail(principal.getName()).orElseThrow().getId();
        return transactionRepository.findByBuyerIdOrSellerId(userId, userId);
    }
}
