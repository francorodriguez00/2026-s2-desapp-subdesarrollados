package com.furbo.controller;

import com.furbo.model.Portfolio;
import com.furbo.model.Transaction;
import com.furbo.repository.PortfolioRepository;
import com.furbo.repository.TransactionRepository;
import com.furbo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired private PortfolioRepository portfolioRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private UserRepository userRepository;

    @GetMapping("/portfolio")
    public List<Portfolio> getPortfolio(Principal principal) {
        Long userId = userRepository.findByEmail(principal.getName()).orElseThrow().getId();
        // Nota: Adaptar a query real por usuario en Repositorio
        return portfolioRepository.findAll().stream().filter(p -> p.getUser().getId().equals(userId)).toList();
    }

    @GetMapping("/transactions")
    public List<Transaction> getTransactions(Principal principal) {
        Long userId = userRepository.findByEmail(principal.getName()).orElseThrow().getId();
        return transactionRepository.findByBuyerIdOrSellerId(userId, userId);
    }
}
