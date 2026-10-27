package com.furbo.controller;

import com.furbo.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired private OrderService orderService;
    @Autowired private com.furbo.repository.UserRepository userRepository;

    @PostMapping("/sell")
    public ResponseEntity<?> sell(@RequestBody SellRequest request, Principal principal) {
        Long userId = userRepository.findByEmail(principal.getName()).orElseThrow().getId();
        orderService.sell(userId, request.playerId(), request.quantity());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/buy")
    public ResponseEntity<?> buy(@RequestBody BuyRequest request, Principal principal) {
        Long userId = userRepository.findByEmail(principal.getName()).orElseThrow().getId();
        orderService.buy(userId, request.playerId(), request.quantity());
        return ResponseEntity.ok().build();
    }

    public record SellRequest(Long playerId, int quantity) {}
    public record BuyRequest(Long playerId, int quantity) {}
}
