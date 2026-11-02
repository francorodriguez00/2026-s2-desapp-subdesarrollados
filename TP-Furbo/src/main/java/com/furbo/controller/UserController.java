package com.furbo.controller;

import com.furbo.adapter.dto.PortfolioResponseDTO;
import com.furbo.adapter.dto.TransactionHistoryDTO;
import java.util.Comparator;
import java.util.Map;
import java.util.ArrayList;
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
    public List<TransactionHistoryDTO> getTransactions(
            Principal principal,
            @RequestParam(required = false) String type, // "COMPRA" o "VENTA"
            @RequestParam(required = false) String playerName,
            @RequestParam(defaultValue = "DESC") String sort) { // "ASC" o "DESC"

        Long userId = userRepository.findByEmail(principal.getName()).orElseThrow().getId();
        List<Transaction> all = transactionRepository.findByBuyerIdOrSellerId(userId, userId);

        Map<String, List<Transaction>> grouped = all.stream()
                .filter(t -> (type == null || type.isEmpty() || (type.equals("COMPRA") ? t.getBuyerId().equals(userId) : t.getSellerId().equals(userId))))
                .filter(t -> (playerName == null || playerName.isEmpty() || playerRepository.findById(t.getPlayerId()).map(p -> p.getName().toLowerCase().contains(playerName.toLowerCase())).orElse(false)))
                .collect(Collectors.groupingBy(Transaction::getOperationId));

        List<TransactionHistoryDTO> history = grouped.entrySet().stream().map(entry -> {
            List<Transaction> txs = entry.getValue();
            Transaction first = txs.get(0);
            String pName = playerRepository.findById(first.getPlayerId()).map(Player::getName).orElse("Desconocido");
            boolean isBuy = first.getBuyerId().equals(userId);
            
            List<TransactionHistoryDTO.TransactionDetailDTO> details = txs.stream()
                    .map(t -> new TransactionHistoryDTO.TransactionDetailDTO(t.getQuantity(), t.getUnitPrice()))
                    .collect(Collectors.toList());

            BigDecimal totalQty = BigDecimal.valueOf(txs.stream().mapToInt(Transaction::getQuantity).sum());
            BigDecimal totalAmt = txs.stream().map(t -> t.getUnitPrice().multiply(BigDecimal.valueOf(t.getQuantity()))).reduce(BigDecimal.ZERO, BigDecimal::add);

            return new TransactionHistoryDTO(
                    entry.getKey(), pName, totalQty.intValue(), totalAmt, first.getCreatedAt(),
                    isBuy ? "COMPRA" : "VENTA", details
            );
        }).collect(Collectors.toList());

        Comparator<TransactionHistoryDTO> comp = Comparator.comparing(TransactionHistoryDTO::getCreatedAt);
        if ("DESC".equalsIgnoreCase(sort)) comp = comp.reversed();
        history.sort(comp);

        return history;
    }
}
