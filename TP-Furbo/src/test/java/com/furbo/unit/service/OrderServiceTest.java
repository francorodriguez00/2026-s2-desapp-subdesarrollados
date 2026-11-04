package com.furbo.unit.service;

import com.furbo.model.*;
import com.furbo.repository.*;
import com.furbo.service.OrderService;
import com.furbo.service.PriceProvider;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;


import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private TradeRepository tradeRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;
    @Mock private PortfolioRepository portfolioRepository;
    @Mock private PosicionPorJugadorRepository posicionRepository;
    @Mock private PlayerRepository playerRepository;
    @Mock private PriceProvider priceProvider;

    @InjectMocks
    private OrderService orderService;

    private Order createBuyOrder(
            Long userId,
            Long orderId,
            int quantity,
            int originalQuantity) {

        Order order = new Order();
        order.setId(orderId);
        order.setUserId(userId);
        order.setPlayerId(100L);
        order.setSide(Order.OrderSide.BUY);
        order.setType(Order.OrderType.LIMIT);
        order.setStatus(Order.OrderStatus.OPEN);
        order.setPrice(new BigDecimal("10.00"));
        order.setQuantityOriginal(originalQuantity);
        order.setQuantityRemaining(quantity);
        order.setReservedAmount(new BigDecimal("50.25"));

        return order;
    }

    private void prepareSellOrder(
            User seller,
            Portfolio sellerPortfolio,
            PosicionPorJugador sellerPosition,
            List<Order> buyOrders) {

        when(priceProvider.getCurrentPrice(100L))
                .thenReturn(new BigDecimal("10.00"));

        when(playerRepository.findByIdWithLock(100L))
                .thenReturn(Optional.of(new Player()));

        when(userRepository.findByIdWithLock(seller.getId()))
                .thenReturn(Optional.of(seller));

        when(portfolioRepository.findByUserWithLock(seller))
                .thenReturn(Optional.of(sellerPortfolio));

        when(posicionRepository.findByPortfolioIdAndPlayerIdWithLock(
                sellerPortfolio.getId(), 100L))
                .thenReturn(Optional.of(sellerPosition));

        when(orderRepository.findOpenBuyOrdersForPlayer(100L))
                .thenReturn(buyOrders);

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> {
                    Order order = invocation.getArgument(0);
                    if (order.getId() == null) {
                        order.setId(200L);
                    }
                    return order;
                });
    }

    @Test
    void createOrder_InsufficientFunds_ThrowsException() {
        User buyer = new User();
        buyer.setId(1L);
        buyer.setBalance(new BigDecimal("5.00"));
        buyer.setReservedBalance(BigDecimal.ZERO);

        when(userRepository.findByIdWithLock(1L)).thenReturn(Optional.of(buyer));
        when(playerRepository.findByIdWithLock(100L)).thenReturn(Optional.of(new Player()));
        when(priceProvider.getCurrentPrice(100L)).thenReturn(new BigDecimal("10.00"));
        when(portfolioRepository.findByUserWithLock(buyer)).thenReturn(Optional.of(new Portfolio()));

        assertThrows(IllegalStateException.class, () -> 
            orderService.createOrder(1L, 100L, Order.OrderSide.BUY, Order.OrderType.LIMIT, new BigDecimal("10.00"), 1)
        );
    }

    @Test
    void createOrder_PriceOutOfBounds_ThrowsException() {
        when(priceProvider.getCurrentPrice(100L))
                .thenReturn(new BigDecimal("10.00"));

        assertThrows(IllegalArgumentException.class, () ->
                orderService.createOrder(
                        1L, 100L,
                        Order.OrderSide.BUY,
                        Order.OrderType.LIMIT,
                        new BigDecimal("20.00"),
                        1
                )
        );

        verifyNoInteractions(userRepository, portfolioRepository);
    }

    @Test
    void cancelOrder_OrderNotFound_ThrowsException() {
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> orderService.cancelOrder(1L, 1L));

        verify(userRepository, never()).findByIdWithLock(anyLong());
    }

    @Test
    void cancelOrder_NotOwner_ThrowsException() {
        Order order = new Order();
        order.setUserId(2L);
        order.setPlayerId(100L);

        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(order));

        assertThrows(
                org.springframework.security.access.AccessDeniedException.class,
                () -> orderService.cancelOrder(1L, 1L)
        );

        verify(playerRepository, never()).findByIdWithLock(anyLong());
    }

    @Test
    void cancelOrder_FilledOrder_ThrowsException() {
        Order order = new Order();
        order.setUserId(1L);
        order.setStatus(Order.OrderStatus.FILLED);

        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class,
                () -> orderService.cancelOrder(1L, 1L));

        verify(playerRepository, never()).findByIdWithLock(anyLong());
    }

    @Test
    void cancelOrder_AlreadyCancelled_ThrowsException() {
        Order order = new Order();
        order.setUserId(1L);
        order.setStatus(Order.OrderStatus.CANCELLED);

        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class,
                () -> orderService.cancelOrder(1L, 1L));

        verify(playerRepository, never()).findByIdWithLock(anyLong());
    }

    @Test
    void cancelOrder_BuyOrder_ReleasesReservedBalance() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setPlayerId(100L);
        order.setSide(Order.OrderSide.BUY);
        order.setStatus(Order.OrderStatus.OPEN);
        order.setPrice(new BigDecimal("10.00"));
        order.setQuantityRemaining(2);
        order.setReservedAmount(new BigDecimal("20.10"));

        User user = new User();
        user.setId(1L);
        user.setReservedBalance(new BigDecimal("20.10"));

        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(order));
        when(playerRepository.findByIdWithLock(100L)).thenReturn(Optional.of(new Player()));
        when(userRepository.findByIdWithLock(1L)).thenReturn(Optional.of(user));

        orderService.cancelOrder(1L, 1L);

        assertEquals(0, user.getReservedBalance().compareTo(BigDecimal.ZERO));
        assertEquals(0, order.getReservedAmount().compareTo(BigDecimal.ZERO));
        assertEquals(0, order.getQuantityRemaining());
        assertEquals(Order.OrderStatus.CANCELLED, order.getStatus());

        verify(userRepository).save(user);
        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrder_BuyOrder_RefundDoesNotExceedReservedAmount() {
        Order order = new Order();
        order.setUserId(1L);
        order.setPlayerId(100L);
        order.setSide(Order.OrderSide.BUY);
        order.setStatus(Order.OrderStatus.OPEN);
        order.setPrice(new BigDecimal("10.00"));
        order.setQuantityRemaining(2);
        order.setReservedAmount(new BigDecimal("10.00"));

        User user = new User();
        user.setId(1L);
        user.setReservedBalance(new BigDecimal("10.00"));

        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(order));
        when(playerRepository.findByIdWithLock(100L)).thenReturn(Optional.of(new Player()));
        when(userRepository.findByIdWithLock(1L)).thenReturn(Optional.of(user));

        orderService.cancelOrder(1L, 1L);

        assertEquals(0, user.getReservedBalance().compareTo(BigDecimal.ZERO));
        assertEquals(0, order.getReservedAmount().compareTo(BigDecimal.ZERO));
    }

    @Test
    void cancelOrder_SellOrder_ReleasesReservedTokens() {
        Order order = new Order();
        order.setUserId(1L);
        order.setPlayerId(100L);
        order.setSide(Order.OrderSide.SELL);
        order.setStatus(Order.OrderStatus.PARTIAL);
        order.setQuantityRemaining(2);

        User user = new User();
        user.setId(1L);

        Portfolio portfolio = new Portfolio();
        portfolio.setId(10L);

        PosicionPorJugador position = new PosicionPorJugador();
        position.setReservedQuantity(5);
        position.setCantidad(10);

        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(order));
        when(playerRepository.findByIdWithLock(100L)).thenReturn(Optional.of(new Player()));
        when(userRepository.findByIdWithLock(1L)).thenReturn(Optional.of(user));
        when(portfolioRepository.findByUserWithLock(user)).thenReturn(Optional.of(portfolio));
        when(posicionRepository.findByPortfolioIdAndPlayerIdWithLock(10L, 100L))
                .thenReturn(Optional.of(position));

        orderService.cancelOrder(1L, 1L);

        assertEquals(3, position.getReservedQuantity());
        assertEquals(10, position.getCantidad());
        assertEquals(0, order.getQuantityRemaining());
        assertEquals(Order.OrderStatus.CANCELLED, order.getStatus());

        verify(posicionRepository).save(position);
        verify(orderRepository).save(order);
    }

    @Test
    void getLastTrade_TradeExists_ReturnsPrice() {
        Trade trade = new Trade();
        trade.setPrice(new BigDecimal("125.50"));

        when(tradeRepository.findTopByPlayerIdOrderByCreatedAtDesc(100L))
                .thenReturn(Optional.of(trade));

        BigDecimal result = orderService.getLastTrade(100L);

        assertEquals(new BigDecimal("125.50"), result);
    }

    @Test
    void getLastTrade_NoTrades_ReturnsNull() {
        when(tradeRepository.findTopByPlayerIdOrderByCreatedAtDesc(100L))
                .thenReturn(Optional.empty());

        BigDecimal result = orderService.getLastTrade(100L);

        assertNull(result);
    }

    private Order createBookOrder(
            Long userId,
            Order.OrderSide side,
            String price,
            int quantity) {

        Order order = new Order();
        order.setUserId(userId);
        order.setPlayerId(100L);
        order.setSide(side);
        order.setPrice(new BigDecimal(price));
        order.setQuantityRemaining(quantity);

        return order;
    }

    @Test
    void getOrderBook_AggregatesAndSortsLevels() {
        Order buy1 = createBookOrder(1L, Order.OrderSide.BUY, "10.00", 3);
        Order buy2 = createBookOrder(2L, Order.OrderSide.BUY, "12.00", 2);
        Order buy3 = createBookOrder(3L, Order.OrderSide.BUY, "10.00", 4);

        Order sell1 = createBookOrder(4L, Order.OrderSide.SELL, "15.00", 5);
        Order sell2 = createBookOrder(5L, Order.OrderSide.SELL, "12.00", 3);
        Order sell3 = createBookOrder(6L, Order.OrderSide.SELL, "15.00", 2);

        when(tradeRepository.findTopByPlayerIdOrderByCreatedAtDesc(100L))
                .thenReturn(Optional.empty());

        when(priceProvider.getCurrentPrice(100L))
                .thenReturn(new BigDecimal("13.00"));

        when(orderRepository.findOpenBuyOrdersForPlayer(100L))
                .thenReturn(List.of(buy1, buy2, buy3));

        when(orderRepository.findOpenSellOrdersForPlayer(100L))
                .thenReturn(List.of(sell1, sell2, sell3));

        when(userRepository.findByIsSuperuserTrue()).thenReturn(Optional.empty());

        OrderService.BookResponseDTO result = orderService.getOrderBook(100L);

        assertEquals(new BigDecimal("13.00"), result.lastPrice());

        assertEquals(2, result.buyLevels().size());
        assertEquals(new BigDecimal("12.00"), result.buyLevels().get(0).price());
        assertEquals(2, result.buyLevels().get(0).totalQuantity());
        assertEquals(new BigDecimal("10.00"), result.buyLevels().get(1).price());
        assertEquals(7, result.buyLevels().get(1).totalQuantity());

        assertEquals(2, result.sellLevels().size());
        assertEquals(new BigDecimal("12.00"), result.sellLevels().get(0).price());
        assertEquals(3, result.sellLevels().get(0).totalQuantity());
        assertEquals(new BigDecimal("15.00"), result.sellLevels().get(1).price());
        assertEquals(7, result.sellLevels().get(1).totalQuantity());
    }

    @Test
    void getOrderBook_UsesLastTradePriceWhenAvailable() {
        Trade trade = new Trade();
        trade.setPrice(new BigDecimal("150.00"));

        when(tradeRepository.findTopByPlayerIdOrderByCreatedAtDesc(100L))
                .thenReturn(Optional.of(trade));

        when(orderRepository.findOpenBuyOrdersForPlayer(100L))
                .thenReturn(List.of());

        when(orderRepository.findOpenSellOrdersForPlayer(100L))
                .thenReturn(List.of());

        when(userRepository.findByIsSuperuserTrue()).thenReturn(Optional.empty());

        OrderService.BookResponseDTO result = orderService.getOrderBook(100L);

        assertEquals(new BigDecimal("150.00"), result.lastPrice());

        verify(priceProvider, never()).getCurrentPrice(100L);
    }

    @Test
    void getOrderBook_LimitsLevelsToFive() {
        List<Order> buys = List.of(
                createBookOrder(1L, Order.OrderSide.BUY, "10.00", 1),
                createBookOrder(2L, Order.OrderSide.BUY, "11.00", 1),
                createBookOrder(3L, Order.OrderSide.BUY, "12.00", 1),
                createBookOrder(4L, Order.OrderSide.BUY, "13.00", 1),
                createBookOrder(5L, Order.OrderSide.BUY, "14.00", 1),
                createBookOrder(6L, Order.OrderSide.BUY, "15.00", 1)
        );

        when(tradeRepository.findTopByPlayerIdOrderByCreatedAtDesc(100L))
                .thenReturn(Optional.empty());
        when(priceProvider.getCurrentPrice(100L)).thenReturn(new BigDecimal("10.00"));
        when(orderRepository.findOpenBuyOrdersForPlayer(100L)).thenReturn(buys);
        when(orderRepository.findOpenSellOrdersForPlayer(100L)).thenReturn(List.of());
        when(userRepository.findByIsSuperuserTrue()).thenReturn(Optional.empty());

        OrderService.BookResponseDTO result = orderService.getOrderBook(100L);

        assertEquals(5, result.buyLevels().size());
        assertEquals(new BigDecimal("15.00"), result.buyLevels().get(0).price());
        assertEquals(new BigDecimal("11.00"), result.buyLevels().get(4).price());
    }

    @Test
    void matchSell_NoBuyOrders_LeavesOrderOpen() {
        User seller = new User();
        seller.setId(2L);

        Portfolio portfolio = new Portfolio();
        portfolio.setId(20L);

        PosicionPorJugador position = new PosicionPorJugador();
        position.setCantidad(10);
        position.setReservedQuantity(0);

        prepareSellOrder(seller, portfolio, position, List.of());

        OrderService.OrderResultDTO result = orderService.createOrder(
                2L, 100L,
                Order.OrderSide.SELL,
                Order.OrderType.LIMIT,
                new BigDecimal("10.00"), 3
        );

        assertEquals(Order.OrderStatus.OPEN, result.order().getStatus());
        assertEquals(3, result.order().getQuantityRemaining());
        assertTrue(result.trades().isEmpty());
        assertEquals(3, position.getReservedQuantity());
    }

    @Test
    void matchSell_BuyPriceTooLow_LeavesOrderOpen() {
        User seller = new User();
        seller.setId(2L);

        Portfolio portfolio = new Portfolio();
        portfolio.setId(20L);

        PosicionPorJugador position = new PosicionPorJugador();
        position.setCantidad(10);
        position.setReservedQuantity(0);

        Order buyOrder = createBuyOrder(1L, 300L, 3, 3);
        buyOrder.setPrice(new BigDecimal("9.00"));

        prepareSellOrder(seller, portfolio, position, List.of(buyOrder));

        OrderService.OrderResultDTO result = orderService.createOrder(
                2L, 100L,
                Order.OrderSide.SELL,
                Order.OrderType.LIMIT,
                new BigDecimal("10.00"), 3
        );

        assertEquals(Order.OrderStatus.OPEN, result.order().getStatus());
        assertEquals(3, result.order().getQuantityRemaining());
        assertTrue(result.trades().isEmpty());
    }

    @Test
    void matchSell_SameUser_DoesNotExecuteTrade() {
        User seller = new User();
        seller.setId(2L);

        Portfolio portfolio = new Portfolio();
        portfolio.setId(20L);

        PosicionPorJugador position = new PosicionPorJugador();
        position.setCantidad(10);
        position.setReservedQuantity(0);

        Order buyOrder = createBuyOrder(2L, 300L, 3, 3);

        prepareSellOrder(seller, portfolio, position, List.of(buyOrder));

        OrderService.OrderResultDTO result = orderService.createOrder(
                2L, 100L,
                Order.OrderSide.SELL,
                Order.OrderType.LIMIT,
                new BigDecimal("10.00"), 3
        );

        assertEquals(Order.OrderStatus.OPEN, result.order().getStatus());
        assertEquals(3, result.order().getQuantityRemaining());
        assertTrue(result.trades().isEmpty());

        verify(transactionRepository, never()).save(any());
        verify(tradeRepository, never()).save(any());
    }

    @Test
    void matchSell_FullExecution_FillsSellOrder() {
        User seller = new User();
        seller.setId(2L);
        seller.setBalance(new BigDecimal("100.00"));

        User buyer = new User();
        buyer.setId(1L);
        buyer.setBalance(new BigDecimal("100.00"));
        buyer.setReservedBalance(new BigDecimal("50.25"));

        Portfolio sellerPortfolio = new Portfolio();
        sellerPortfolio.setId(20L);

        Portfolio buyerPortfolio = new Portfolio();
        buyerPortfolio.setId(10L);

        PosicionPorJugador sellerPosition = new PosicionPorJugador();
        sellerPosition.setCantidad(10);
        sellerPosition.setReservedQuantity(0);

        PosicionPorJugador buyerPosition = new PosicionPorJugador();
        buyerPosition.setCantidad(0);

        Order buyOrder = createBuyOrder(1L, 300L, 2, 5);

        prepareSellOrder(seller, sellerPortfolio, sellerPosition, List.of(buyOrder));

        when(userRepository.findByIdWithLock(1L)).thenReturn(Optional.of(buyer));
        when(portfolioRepository.findByUserWithLock(buyer)).thenReturn(Optional.of(buyerPortfolio));

        when(posicionRepository.findByPortfolioIdAndPlayerIdWithLock(10L, 100L))
                .thenReturn(Optional.of(buyerPosition));

        when(tradeRepository.save(any(Trade.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderService.OrderResultDTO result = orderService.createOrder(
                2L, 100L,
                Order.OrderSide.SELL,
                Order.OrderType.LIMIT,
                new BigDecimal("10.00"), 2
        );

        assertEquals(Order.OrderStatus.FILLED, result.order().getStatus());
        assertEquals(0, result.order().getQuantityRemaining());

        assertEquals(1, result.trades().size());
        assertEquals(2, result.trades().get(0).getQuantity());
        assertEquals(new BigDecimal("10.00"), result.trades().get(0).getPrice());

        assertEquals(8, sellerPosition.getCantidad());
        assertEquals(0, sellerPosition.getReservedQuantity());

        assertEquals(2, buyerPosition.getCantidad());

        assertEquals(new BigDecimal("119.90"), seller.getBalance());
        assertEquals(new BigDecimal("79.90"), buyer.getBalance());

        assertEquals(Order.OrderStatus.FILLED, buyOrder.getStatus());
        assertEquals(0, buyOrder.getQuantityRemaining());

        verify(transactionRepository).save(any(Transaction.class));
        verify(tradeRepository).save(any(Trade.class));
    }

    @Test
    void matchSell_PartialExecution_LeavesRemainingQuantity() {
        User seller = new User();
        seller.setId(2L);
        seller.setBalance(new BigDecimal("100.00"));

        User buyer = new User();
        buyer.setId(1L);
        buyer.setBalance(new BigDecimal("100.00"));
        buyer.setReservedBalance(new BigDecimal("50.25"));

        Portfolio sellerPortfolio = new Portfolio();
        sellerPortfolio.setId(20L);

        Portfolio buyerPortfolio = new Portfolio();
        buyerPortfolio.setId(10L);

        PosicionPorJugador sellerPosition = new PosicionPorJugador();
        sellerPosition.setCantidad(10);
        sellerPosition.setReservedQuantity(0);

        PosicionPorJugador buyerPosition = new PosicionPorJugador();
        buyerPosition.setCantidad(0);

        Order buyOrder = createBuyOrder(1L, 300L, 2, 5);

        prepareSellOrder(seller, sellerPortfolio, sellerPosition, List.of(buyOrder));

        when(userRepository.findByIdWithLock(1L)).thenReturn(Optional.of(buyer));
        when(portfolioRepository.findByUserWithLock(buyer)).thenReturn(Optional.of(buyerPortfolio));

        when(posicionRepository.findByPortfolioIdAndPlayerIdWithLock(10L, 100L))
                .thenReturn(Optional.of(buyerPosition));

        when(tradeRepository.save(any(Trade.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderService.OrderResultDTO result = orderService.createOrder(
                2L, 100L,
                Order.OrderSide.SELL,
                Order.OrderType.LIMIT,
                new BigDecimal("10.00"), 5
        );

        assertEquals(Order.OrderStatus.PARTIAL, result.order().getStatus());
        assertEquals(3, result.order().getQuantityRemaining());

        assertEquals(1, result.trades().size());
        assertEquals(2, result.trades().get(0).getQuantity());

        assertEquals(8, sellerPosition.getCantidad());
        assertEquals(3, sellerPosition.getReservedQuantity());

        assertEquals(2, buyerPosition.getCantidad());

        assertEquals(Order.OrderStatus.FILLED, buyOrder.getStatus());
        assertEquals(0, buyOrder.getQuantityRemaining());
    }
}
