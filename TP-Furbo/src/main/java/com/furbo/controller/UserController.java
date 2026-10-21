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
    public Portfolio getPortfolio(Principal principal) {
        com.furbo.model.User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        return portfolioRepository.findByUser(user).orElseGet(() -> {
            Portfolio p = new Portfolio();
            p.setUser(user);
            return portfolioRepository.save(p);
        });
    }

    @GetMapping("/transactions")
    public List<Transaction> getTransactions(Principal principal) {
        Long userId = userRepository.findByEmail(principal.getName()).orElseThrow().getId();
        return transactionRepository.findByBuyerIdOrSellerId(userId, userId);
    }
}
