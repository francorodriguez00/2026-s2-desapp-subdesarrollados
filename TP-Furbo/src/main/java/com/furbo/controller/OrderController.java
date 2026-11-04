package com.furbo.controller;

import com.furbo.model.Order;
import com.furbo.service.OrderService;
import com.furbo.repository.UserRepository;
import com.furbo.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired private OrderService orderService;
    @Autowired private UserRepository userRepository;
    @Autowired private OrderRepository orderRepository;

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest request, Principal principal) {
        Long userId = userRepository.findByEmail(principal.getName()).orElseThrow().getId();
        OrderService.OrderResultDTO result = orderService.createOrder(
                userId, request.playerId(), request.side(), request.type(), request.price(), request.quantity()
        );
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id, Principal principal) {
        Long userId = userRepository.findByEmail(principal.getName()).orElseThrow().getId();
        orderService.cancelOrder(userId, id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyOrders(Principal principal) {
        Long userId = userRepository.findByEmail(principal.getName()).orElseThrow().getId();
        List<Order> orders = orderRepository.findByUserIdAndStatusIn(userId, List.of(Order.OrderStatus.OPEN, Order.OrderStatus.PARTIAL));
        return ResponseEntity.ok(orders);
    }

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

    public record OrderRequest(
            Long playerId,
            Order.OrderSide side,
            Order.OrderType type,
            BigDecimal price,
            int quantity
    ) {}

    public record SellRequest(Long playerId, int quantity) {}
    public record BuyRequest(Long playerId, int quantity) {}
}
