package com.furbo.service;

import com.furbo.model.*;
import com.furbo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private TradeRepository tradeRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PortfolioRepository portfolioRepository;
    @Autowired private PosicionPorJugadorRepository posicionRepository;
    @Autowired private PlayerRepository playerRepository;
    @Autowired private PriceProvider priceProvider;

    @Transactional
    public OrderResultDTO createOrder(Long userId, Long playerId, Order.OrderSide side, Order.OrderType type, BigDecimal inputPrice, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }

        BigDecimal currentPrice = priceProvider.getCurrentPrice(playerId);
        BigDecimal price = inputPrice;

        if (type == Order.OrderType.MARKET) {
            price = currentPrice;
        } else {
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0 || price.scale() > 2) {
                throw new IllegalArgumentException("El precio debe ser mayor a 0 y tener máximo 2 decimales");
            }
            BigDecimal minPrice = currentPrice.multiply(BigDecimal.valueOf(0.5));
            BigDecimal maxPrice = currentPrice.multiply(BigDecimal.valueOf(1.5));
            if (price.compareTo(minPrice) < 0 || price.compareTo(maxPrice) > 0) {
                throw new IllegalArgumentException("Precio fuera de banda permitida (+-50% del mercado)");
            }
        }

        playerRepository.findByIdWithLock(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Jugador no encontrado"));

        User user = userRepository.findByIdWithLock(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        Portfolio portfolio = portfolioRepository.findByUserWithLock(user)
                .orElseGet(() -> {
                    Portfolio p = new Portfolio();
                    p.setUser(user);
                    return portfolioRepository.save(p);
                });

        BigDecimal reservedAmount = BigDecimal.ZERO;

        if (side == Order.OrderSide.BUY) {
            BigDecimal totalValue = price.multiply(BigDecimal.valueOf(quantity));
            BigDecimal maxCommission = totalValue.multiply(BigDecimal.valueOf(0.005)).setScale(2, RoundingMode.CEILING);
            reservedAmount = totalValue.add(maxCommission);

            BigDecimal availableBalance = user.getBalance().subtract(user.getReservedBalance());
            if (availableBalance.compareTo(reservedAmount) < 0) {
                throw new IllegalStateException("Saldo insuficiente");
            }

            user.setReservedBalance(user.getReservedBalance().add(reservedAmount));
            userRepository.save(user);
        } else {
            PosicionPorJugador position = posicionRepository.findByPortfolioIdAndPlayerIdWithLock(portfolio.getId(), playerId)
                    .orElseThrow(() -> new IllegalArgumentException("No posee tenencia de este jugador"));

            int availableTokens = position.getCantidad() - position.getReservedQuantity();
            if (availableTokens < quantity) {
                throw new IllegalStateException("Tokens insuficientes o ya reservados");
            }

            position.setReservedQuantity(position.getReservedQuantity() + quantity);
            posicionRepository.save(position);
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setPlayerId(playerId);
        order.setSide(side);
        order.setType(type);
        order.setStatus(Order.OrderStatus.OPEN);
        order.setPrice(price);
        order.setQuantityOriginal(quantity);
        order.setQuantityRemaining(quantity);
        order.setReservedAmount(reservedAmount);
        order.setCreatedAt(LocalDateTime.now());

        order = orderRepository.save(order);

        List<Trade> trades = matchOrder(order, java.util.UUID.randomUUID().toString());

        if (side == Order.OrderSide.BUY && type == Order.OrderType.MARKET && order.getQuantityRemaining() > 0) {
            throw new IllegalStateException("Stock insuficiente");
        }

        return new OrderResultDTO(order, trades);
    }

    @Transactional
    public void buy(Long buyerId, Long playerId, int quantity) {
        createOrder(buyerId, playerId, Order.OrderSide.BUY, Order.OrderType.MARKET, null, quantity);
    }

    @Transactional
    public void sell(Long sellerId, Long playerId, int quantity) {
        BigDecimal currentPrice = priceProvider.getCurrentPrice(playerId);
        createOrder(sellerId, playerId, Order.OrderSide.SELL, Order.OrderType.LIMIT, currentPrice, quantity);
    }

    @Transactional
    public void cancelOrder(Long userId, Long orderId) {
        Order order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));

        if (!order.getUserId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("No autorizado para cancelar esta orden");
        }

        if (order.getStatus() == Order.OrderStatus.FILLED || order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new IllegalStateException("La orden ya se encuentra cerrada o cancelada");
        }

        playerRepository.findByIdWithLock(order.getPlayerId()).orElseThrow();

        releaseOrderReservation(order);

        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setQuantityRemaining(0);
        orderRepository.save(order);
    }

    public BookResponseDTO getOrderBook(Long playerId) {
        BigDecimal lastPrice = tradeRepository.findTopByPlayerIdOrderByCreatedAtDesc(playerId)
                .map(Trade::getPrice)
                .orElseGet(() -> priceProvider.getCurrentPrice(playerId));

        List<Order> buyOrders = orderRepository.findOpenBuyOrdersForPlayer(playerId);
        Map<BigDecimal, Integer> buyMap = new LinkedHashMap<>();
        for (Order o : buyOrders) {
            buyMap.put(o.getPrice(), buyMap.getOrDefault(o.getPrice(), 0) + o.getQuantityRemaining());
        }
        List<BookLevelDTO> buyLevels = buyMap.entrySet().stream()
                .sorted(Map.Entry.<BigDecimal, Integer>comparingByKey().reversed())
                .limit(5)
                .map(e -> new BookLevelDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        List<SellSource> sellSources = getAvailableSellSources(playerId, BigDecimal.valueOf(Double.MAX_VALUE));
        Map<BigDecimal, Integer> sellMap = new TreeMap<>();
        for (SellSource s : sellSources) {
            sellMap.put(s.price, sellMap.getOrDefault(s.price, 0) + s.remainingQuantity);
        }
        List<BookLevelDTO> sellLevels = sellMap.entrySet().stream()
                .limit(5)
                .map(e -> new BookLevelDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        return new BookResponseDTO(buyLevels, sellLevels, lastPrice);
    }

    public BigDecimal getLastTrade(Long playerId) {
        return tradeRepository.findTopByPlayerIdOrderByCreatedAtDesc(playerId)
                .map(Trade::getPrice)
                .orElse(null);
    }

    private void releaseOrderReservation(Order order) {
        User user = userRepository.findByIdWithLock(order.getUserId()).orElseThrow();
        if (order.getSide() == Order.OrderSide.BUY) {
            BigDecimal unitTotal = order.getPrice().multiply(BigDecimal.valueOf(1.005));
            BigDecimal refund = unitTotal.multiply(BigDecimal.valueOf(order.getQuantityRemaining())).setScale(2, RoundingMode.HALF_UP);
            if (refund.compareTo(order.getReservedAmount()) > 0) {
                refund = order.getReservedAmount();
            }
            user.setReservedBalance(user.getReservedBalance().subtract(refund));
            order.setReservedAmount(order.getReservedAmount().subtract(refund));
            userRepository.save(user);
        } else {
            Portfolio portfolio = portfolioRepository.findByUserWithLock(user).orElseThrow();
            PosicionPorJugador pos = posicionRepository.findByPortfolioIdAndPlayerIdWithLock(portfolio.getId(), order.getPlayerId()).orElseThrow();
            pos.setReservedQuantity(pos.getReservedQuantity() - order.getQuantityRemaining());
            posicionRepository.save(pos);
        }
    }

    private List<Trade> matchOrder(Order order, String operationId) {
        List<Trade> trades = new ArrayList<>();
        Long playerId = order.getPlayerId();

        if (order.getSide() == Order.OrderSide.BUY) {
            while (order.getQuantityRemaining() > 0) {
                List<SellSource> sellSources = getAvailableSellSources(playerId, order.getPrice());
                if (sellSources.isEmpty()) break;

                SellSource bestSource = sellSources.get(0);
                if (bestSource.price.compareTo(order.getPrice()) > 0) break;

                int canTake = Math.min(order.getQuantityRemaining(), bestSource.remainingQuantity);
                if (canTake <= 0) break;

                Trade trade = executeTrade(order, bestSource, canTake, bestSource.price, java.util.UUID.randomUUID().toString());
                trades.add(trade);

                order.setQuantityRemaining(order.getQuantityRemaining() - canTake);
                if (order.getQuantityRemaining() == 0) {
                    order.setStatus(Order.OrderStatus.FILLED);
                } else {
                    order.setStatus(Order.OrderStatus.PARTIAL);
                }
                orderRepository.save(order);
            }
        } else {
            while (order.getQuantityRemaining() > 0) {
                List<Order> buyOrders = orderRepository.findOpenBuyOrdersForPlayer(playerId);
                if (buyOrders.isEmpty()) break;

                Order bestBuy = buyOrders.get(0);
                if (bestBuy.getPrice().compareTo(order.getPrice()) < 0) break;

                if (order.getUserId().equals(bestBuy.getUserId())) {
                    break;
                }

                int canTake = Math.min(order.getQuantityRemaining(), bestBuy.getQuantityRemaining());
                if (canTake <= 0) break;

                Trade trade = executeSellTrade(order, bestBuy, canTake, bestBuy.getPrice(), java.util.UUID.randomUUID().toString());
                trades.add(trade);

                order.setQuantityRemaining(order.getQuantityRemaining() - trade.getQuantity());
                if (order.getQuantityRemaining() == 0) {
                    order.setStatus(Order.OrderStatus.FILLED);
                } else {
                    order.setStatus(Order.OrderStatus.PARTIAL);
                }
                orderRepository.save(order);
            }
        }

        return trades;
    }

    private List<SellSource> getAvailableSellSources(Long playerId, BigDecimal maxPrice) {
        List<SellSource> sources = new ArrayList<>();

        List<Order> userSellOrders = orderRepository.findOpenSellOrdersForPlayer(playerId);
        for (Order o : userSellOrders) {
            sources.add(new SellSource(o.getUserId(), o.getId(), o.getPrice(), o.getQuantityRemaining(), false, o));
        }

        Optional<User> superuserOpt = userRepository.findByIsSuperuserTrue();
        if (superuserOpt.isPresent()) {
            User superuser = superuserOpt.get();
            Optional<Portfolio> spOpt = portfolioRepository.findByUser(superuser);
            if (spOpt.isPresent()) {
                Optional<PosicionPorJugador> sposOpt = posicionRepository.findByPortfolioIdAndPlayerId(spOpt.get().getId(), playerId);
                if (sposOpt.isPresent()) {
                    PosicionPorJugador spos = sposOpt.get();
                    int availableSuperuserStock = spos.getCantidad() - spos.getReservedQuantity();
                    if (availableSuperuserStock > 0) {
                        BigDecimal currentMarketPrice = priceProvider.getCurrentPrice(playerId);
                        sources.add(new SellSource(superuser.getId(), null, currentMarketPrice, availableSuperuserStock, true, null));
                    }
                }
            }
        }

        sources.sort((a, b) -> {
            int cmp = a.price.compareTo(b.price);
            if (cmp != 0) return cmp;
            if (a.isSuperuser != b.isSuperuser) {
                return a.isSuperuser ? 1 : -1;
            }
            if (a.order != null && b.order != null) {
                return a.order.getCreatedAt().compareTo(b.order.getCreatedAt());
            }
            return 0;
        });

        return sources;
    }

    private Trade executeTrade(Order buyerOrder, SellSource sellerSource, int quantity, BigDecimal executionPrice, String operationId) {
        Long buyerId = buyerOrder.getUserId();
        Long sellerId = sellerSource.userId;

        if (buyerId.equals(sellerId)) {
            throw new IllegalStateException("Auto-operación no permitida");
        }

        List<Long> userIds = Arrays.asList(buyerId, sellerId);
        userIds.sort(Long::compareTo);
        for (Long uid : userIds) {
            userRepository.findByIdWithLock(uid);
        }

        User buyer = userRepository.findByIdWithLock(buyerId).orElseThrow();
        User seller = userRepository.findByIdWithLock(sellerId).orElseThrow();
        Portfolio buyerPortfolio = portfolioRepository.findByUserWithLock(buyer).orElseThrow();
        Portfolio sellerPortfolio = portfolioRepository.findByUserWithLock(seller).orElseThrow();

        BigDecimal totalCost = executionPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal buyerCommission = totalCost.multiply(BigDecimal.valueOf(0.005)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalBuyerPayment = totalCost.add(buyerCommission);

        buyer.setBalance(buyer.getBalance().subtract(totalBuyerPayment));
        BigDecimal unitReserved = buyerOrder.getReservedAmount().divide(BigDecimal.valueOf(buyerOrder.getQuantityOriginal()), 4, RoundingMode.HALF_UP);
        BigDecimal reservedToRelease = unitReserved.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
        buyer.setReservedBalance(buyer.getReservedBalance().subtract(reservedToRelease));
        buyerOrder.setReservedAmount(buyerOrder.getReservedAmount().subtract(reservedToRelease));
        userRepository.save(buyer);

        PosicionPorJugador buyerPos = posicionRepository.findByPortfolioIdAndPlayerIdWithLock(buyerPortfolio.getId(), buyerOrder.getPlayerId())
                .orElseGet(() -> {
                    PosicionPorJugador p = new PosicionPorJugador();
                    p.setPortfolio(buyerPortfolio);
                    p.setPlayerId(buyerOrder.getPlayerId());
                    return p;
                });
        buyerPos.setCantidad(buyerPos.getCantidad() + quantity);
        posicionRepository.save(buyerPos);

        BigDecimal sellerCommission = BigDecimal.ZERO;
        BigDecimal sellerProceeds = totalCost;

        if (!sellerSource.isSuperuser) {
            sellerCommission = totalCost.multiply(BigDecimal.valueOf(0.005)).setScale(2, RoundingMode.HALF_UP);
            sellerProceeds = totalCost.subtract(sellerCommission);
        }

        seller.setBalance(seller.getBalance().add(sellerProceeds));
        userRepository.save(seller);

        PosicionPorJugador sellerPos = posicionRepository.findByPortfolioIdAndPlayerIdWithLock(sellerPortfolio.getId(), buyerOrder.getPlayerId()).orElseThrow();
        sellerPos.setCantidad(sellerPos.getCantidad() - quantity);
        if (!sellerSource.isSuperuser) {
            sellerPos.setReservedQuantity(sellerPos.getReservedQuantity() - quantity);
        }
        posicionRepository.save(sellerPos);

        Long sellerOrderId = null;
        if (!sellerSource.isSuperuser && sellerSource.order != null) {
            Order sellOrder = orderRepository.findByIdWithLock(sellerSource.order.getId()).orElseThrow();
            sellOrder.setQuantityRemaining(sellOrder.getQuantityRemaining() - quantity);
            if (sellOrder.getQuantityRemaining() == 0) {
                sellOrder.setStatus(Order.OrderStatus.FILLED);
            } else {
                sellOrder.setStatus(Order.OrderStatus.PARTIAL);
            }
            orderRepository.save(sellOrder);
            sellerOrderId = sellOrder.getId();
        }

        Transaction tx = new Transaction();
        tx.setBuyerId(buyerId);
        tx.setSellerId(sellerId);
        tx.setPlayerId(buyerOrder.getPlayerId());
        tx.setQuantity(quantity);
        tx.setUnitPrice(executionPrice);
        tx.setCommission(buyerCommission);
        tx.setOperationId(operationId);
        transactionRepository.save(tx);

        Trade trade = new Trade();
        trade.setBuyerOrderId(buyerOrder.getId());
        trade.setSellerOrderId(sellerOrderId != null ? sellerOrderId : 0L);
        trade.setPlayerId(buyerOrder.getPlayerId());
        trade.setPrice(executionPrice);
        trade.setQuantity(quantity);
        trade.setCommissionBuyer(buyerCommission);
        trade.setCommissionSeller(sellerCommission);
        trade.setCreatedAt(LocalDateTime.now());
        return tradeRepository.save(trade);
    }

    private Trade executeSellTrade(Order sellOrder, Order buyerOrder, int quantity, BigDecimal executionPrice, String operationId) {
        Long sellerId = sellOrder.getUserId();
        Long buyerId = buyerOrder.getUserId();

        if (sellerId.equals(buyerId)) {
            throw new IllegalStateException("Auto-operación no permitida");
        }

        List<Long> userIds = Arrays.asList(sellerId, buyerId);
        userIds.sort(Long::compareTo);
        for (Long uid : userIds) {
            userRepository.findByIdWithLock(uid);
        }

        User seller = userRepository.findByIdWithLock(sellerId).orElseThrow();
        User buyer = userRepository.findByIdWithLock(buyerId).orElseThrow();
        Portfolio sellerPortfolio = portfolioRepository.findByUserWithLock(seller).orElseThrow();
        Portfolio buyerPortfolio = portfolioRepository.findByUserWithLock(buyer).orElseThrow();

        BigDecimal totalCost = executionPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal buyerCommission = totalCost.multiply(BigDecimal.valueOf(0.005)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalBuyerPayment = totalCost.add(buyerCommission);

        buyer.setBalance(buyer.getBalance().subtract(totalBuyerPayment));
        BigDecimal unitReserved = buyerOrder.getReservedAmount().divide(BigDecimal.valueOf(buyerOrder.getQuantityOriginal()), 4, RoundingMode.HALF_UP);
        BigDecimal reservedToRelease = unitReserved.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
        buyer.setReservedBalance(buyer.getReservedBalance().subtract(reservedToRelease));
        buyerOrder.setReservedAmount(buyerOrder.getReservedAmount().subtract(reservedToRelease));
        userRepository.save(buyer);
        buyerOrder.setQuantityRemaining(buyerOrder.getQuantityRemaining() - quantity);
        buyerOrder.setStatus(buyerOrder.getQuantityRemaining() == 0 ? Order.OrderStatus.FILLED : Order.OrderStatus.PARTIAL);
        orderRepository.save(buyerOrder);

        PosicionPorJugador buyerPos = posicionRepository.findByPortfolioIdAndPlayerIdWithLock(buyerPortfolio.getId(), sellOrder.getPlayerId())
                .orElseGet(() -> {
                    PosicionPorJugador p = new PosicionPorJugador();
                    p.setPortfolio(buyerPortfolio);
                    p.setPlayerId(sellOrder.getPlayerId());
                    return p;
                });
        buyerPos.setCantidad(buyerPos.getCantidad() + quantity);
        posicionRepository.save(buyerPos);

        BigDecimal sellerCommission = totalCost.multiply(BigDecimal.valueOf(0.005)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal sellerProceeds = totalCost.subtract(sellerCommission);
        seller.setBalance(seller.getBalance().add(sellerProceeds));
        userRepository.save(seller);

        PosicionPorJugador sellerPos = posicionRepository.findByPortfolioIdAndPlayerIdWithLock(sellerPortfolio.getId(), sellOrder.getPlayerId()).orElseThrow();
        sellerPos.setCantidad(sellerPos.getCantidad() - quantity);
        sellerPos.setReservedQuantity(sellerPos.getReservedQuantity() - quantity);
        posicionRepository.save(sellerPos);

        Transaction tx = new Transaction();
        tx.setBuyerId(buyerId);
        tx.setSellerId(sellerId);
        tx.setPlayerId(sellOrder.getPlayerId());
        tx.setQuantity(quantity);
        tx.setUnitPrice(executionPrice);
        tx.setCommission(buyerCommission);
        tx.setOperationId(operationId);
        transactionRepository.save(tx);

        Trade trade = new Trade();
        trade.setBuyerOrderId(buyerOrder.getId());
        trade.setSellerOrderId(sellOrder.getId());
        trade.setPlayerId(sellOrder.getPlayerId());
        trade.setPrice(executionPrice);
        trade.setQuantity(quantity);
        trade.setCommissionBuyer(buyerCommission);
        trade.setCommissionSeller(sellerCommission);
        trade.setCreatedAt(LocalDateTime.now());
        return tradeRepository.save(trade);
    }

    public static class SellSource {
        public Long userId;
        public Long orderId;
        public BigDecimal price;
        public int remainingQuantity;
        public boolean isSuperuser;
        public Order order;

        public SellSource(Long userId, Long orderId, BigDecimal price, int remainingQuantity, boolean isSuperuser, Order order) {
            this.userId = userId;
            this.orderId = orderId;
            this.price = price;
            this.remainingQuantity = remainingQuantity;
            this.isSuperuser = isSuperuser;
            this.order = order;
        }
    }

    public record OrderResultDTO(Order order, List<Trade> trades) {}
    public record BookResponseDTO(List<BookLevelDTO> buyLevels, List<BookLevelDTO> sellLevels, BigDecimal lastPrice) {}
    public record BookLevelDTO(BigDecimal price, int totalQuantity) {}
}
