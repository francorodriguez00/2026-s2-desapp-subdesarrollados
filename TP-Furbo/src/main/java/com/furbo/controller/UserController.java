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
import org.springframework.transaction.annotation.Transactional;
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
                .filter(p -> p.getCantidad() > 0)
                .map(p -> {
                    Player player = playerRepository.findById(p.getPlayerId()).orElse(new Player());
                    BigDecimal price = priceProvider.getCurrentPrice(p.getPlayerId());
                    
                    // Cálculo de precio promedio de compra
                    List<Transaction> compras = transactionRepository.findByBuyerIdAndPlayerId(user.getId(), p.getPlayerId());
                    BigDecimal totalInvertidoJugador = BigDecimal.ZERO;
                    int cantidadComprada = 0;
                    for (Transaction t : compras) {
                        totalInvertidoJugador = totalInvertidoJugador.add(t.getUnitPrice().multiply(BigDecimal.valueOf(t.getQuantity())));
                        cantidadComprada += t.getQuantity();
                    }
                    BigDecimal precioPromedio = cantidadComprada > 0 ? totalInvertidoJugador.divide(BigDecimal.valueOf(cantidadComprada), 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
                    
                    BigDecimal valorTotal = price.multiply(BigDecimal.valueOf(p.getCantidad()));
                    BigDecimal porcentajeGanancia = BigDecimal.ZERO;
                    if (precioPromedio.compareTo(BigDecimal.ZERO) > 0) {
                        porcentajeGanancia = price.subtract(precioPromedio).divide(precioPromedio, 4, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                    }
                    
                    return new PortfolioResponseDTO.PositionDTO(p.getPlayerId(), player.getName(), p.getCantidad(), price, valorTotal, precioPromedio, porcentajeGanancia);
                })
                .collect(Collectors.toList());

        for (PortfolioResponseDTO.PositionDTO pos : posDTOs) {
            totalInvertido = totalInvertido.add(pos.getValorTotal());
        }

        return new PortfolioResponseDTO(posDTOs, user.getBalance(), totalInvertido);
    }

    @PostMapping("/credits/deposit")
    @Transactional
    public void depositCredits(Principal principal, @RequestBody Map<String, BigDecimal> body) {
        BigDecimal amount = body.get("amount");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Monto inválido");
        }
        com.furbo.model.User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        user.setBalance(user.getBalance().add(amount));
        userRepository.save(user);

        Transaction t = new Transaction();
        t.setBuyerId(user.getId());
        t.setSellerId(0L); // Indicador de depósito del sistema
        t.setPlayerId(0L);
        t.setQuantity(0);
        t.setUnitPrice(amount);
        t.setOperationId("DEP-" + java.util.UUID.randomUUID().toString());
        transactionRepository.save(t);
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
                .filter(t -> {
                    if (type == null || type.isEmpty()) return true;
                    if (type.equals("DEPÓSITO")) return t.getPlayerId() == 0L;
                    if (type.equals("COMPRA")) return t.getBuyerId().equals(userId) && t.getPlayerId() != 0L;
                    if (type.equals("VENTA")) return t.getSellerId().equals(userId) && t.getPlayerId() != 0L;
                    return true;
                })
                .filter(t -> (playerName == null || playerName.isEmpty() || playerRepository.findById(t.getPlayerId()).map(p -> p.getName().toLowerCase().contains(playerName.toLowerCase())).orElse(false)))
                .collect(Collectors.groupingBy(Transaction::getOperationId));

        List<TransactionHistoryDTO> history = grouped.entrySet().stream().map(entry -> {
            List<Transaction> txs = entry.getValue();
            Transaction first = txs.get(0);
            String pName = playerRepository.findById(first.getPlayerId()).map(Player::getName).orElse("DEPÓSITO");
            boolean isBuy = first.getBuyerId().equals(userId);
            String opType = first.getPlayerId() == 0L ? "DEPÓSITO" : (isBuy ? "COMPRA" : "VENTA");
            
            List<TransactionHistoryDTO.TransactionDetailDTO> details = txs.stream()
                    .map(t -> new TransactionHistoryDTO.TransactionDetailDTO(t.getQuantity(), t.getUnitPrice(), t.getCommission()))
                    .collect(Collectors.toList());

            BigDecimal totalQty = BigDecimal.valueOf(txs.stream().mapToInt(Transaction::getQuantity).sum());
            BigDecimal totalAmt = txs.stream().map(t -> {
                if (t.getPlayerId() == 0L) return t.getUnitPrice();
                return t.getUnitPrice().multiply(BigDecimal.valueOf(t.getQuantity()));
            }).reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalCommission = txs.stream().map(Transaction::getCommission).reduce(BigDecimal.ZERO, BigDecimal::add);

            return new TransactionHistoryDTO(
                    entry.getKey(), pName, totalQty.intValue(), totalAmt, totalCommission, first.getCreatedAt(),
                    opType, details
            );
        }).collect(Collectors.toList());

        Comparator<TransactionHistoryDTO> comp = Comparator.comparing(TransactionHistoryDTO::getCreatedAt);
        if ("DESC".equalsIgnoreCase(sort)) comp = comp.reversed();
        history.sort(comp);

        return history;
    }
}
