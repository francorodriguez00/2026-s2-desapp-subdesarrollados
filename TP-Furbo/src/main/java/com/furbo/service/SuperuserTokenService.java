package com.furbo.service;

import com.furbo.model.*;
import com.furbo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SuperuserTokenService {

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final PosicionPorJugadorRepository posicionRepository;
    private final PlayerRepository playerRepository;
    private final OrderRepository orderRepository;
    private final PriceProvider priceProvider;

    public SuperuserTokenService(UserRepository userRepository, PortfolioRepository portfolioRepository,
                                 PosicionPorJugadorRepository posicionRepository, PlayerRepository playerRepository,
                                 OrderRepository orderRepository, PriceProvider priceProvider) {
        this.userRepository = userRepository;
        this.portfolioRepository = portfolioRepository;
        this.posicionRepository = posicionRepository;
        this.playerRepository = playerRepository;
        this.orderRepository = orderRepository;
        this.priceProvider = priceProvider;
    }

    @Transactional
    public Portfolio asegurarPortfolioSuperuser() {
        User superuser = userRepository.findByIsSuperuserTrue()
                .orElseThrow(() -> new IllegalStateException("No existe superusuario"));
        
        return portfolioRepository.findByUser(superuser)
                .orElseGet(() -> {
                    Portfolio p = new Portfolio();
                    p.setUser(superuser);
                    return portfolioRepository.save(p);
                });
    }

    @Transactional
    public void asignarTokens(Player jugador) {
        Portfolio portfolio = asegurarPortfolioSuperuser();
        User superuser = portfolio.getUser();

        if (posicionRepository.findByPortfolioIdAndPlayerId(portfolio.getId(), jugador.getId()).isEmpty()) {
            PosicionPorJugador posicion = new PosicionPorJugador();
            posicion.setPortfolio(portfolio);
            posicion.setPlayerId(jugador.getId());
            posicion.setCantidad(100);
            posicion.setReservedQuantity(100);
            posicionRepository.save(posicion);

            BigDecimal currentPrice = priceProvider.getCurrentPrice(jugador.getId());
            Order order = new Order();
            order.setUserId(superuser.getId());
            order.setPlayerId(jugador.getId());
            order.setSide(Order.OrderSide.SELL);
            order.setType(Order.OrderType.LIMIT);
            order.setStatus(Order.OrderStatus.OPEN);
            order.setPrice(currentPrice);
            order.setQuantityOriginal(100);
            order.setQuantityRemaining(100);
            order.setCreatedAt(LocalDateTime.now());
            orderRepository.save(order);
        }
    }

    @Transactional
    public void sincronizarTodos() {
        List<Player> todosLosJugadores = playerRepository.findAll();
        for (Player j : todosLosJugadores) {
            asignarTokens(j);
        }
    }
}
