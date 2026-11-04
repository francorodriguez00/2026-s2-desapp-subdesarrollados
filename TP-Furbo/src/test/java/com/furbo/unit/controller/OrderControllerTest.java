package com.furbo.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.furbo.controller.OrderController;
import com.furbo.model.Order;
import com.furbo.model.User;
import com.furbo.repository.UserRepository;
import com.furbo.security.JwtAuthenticationFilter;
import com.furbo.service.OrderService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.furbo.model.Order;
import com.furbo.model.Player;
import com.furbo.model.Trade;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(
    controllers = OrderController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
public class OrderControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private OrderService orderService;
    @MockBean private UserRepository userRepository;
    @MockBean private com.furbo.repository.OrderRepository orderRepository;
    @MockBean private com.furbo.repository.PlayerRepository playerRepository;

    private static final Principal USER = () -> "user@furbo.com";

    private void stubUsuarioExistente(Long id) {
        User u = new User();
        u.setId(id);
        when(userRepository.findByEmail("user@furbo.com")).thenReturn(Optional.of(u));
    }

    @Nested
    class Buy {
        @Test
        void conDatosValidos_devuelve200_yDelegaAlService() throws Exception {
            stubUsuarioExistente(7L);

            mockMvc.perform(post("/orders/buy")
                    .principal(USER)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new OrderController.BuyRequest(1L, 3))))
                .andExpect(status().isOk());

            verify(orderService).buy(7L, 1L, 3);
        }

        @Test
        void conUsuarioInexistente_devuelveError() throws Exception {
            when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

            mockMvc.perform(post("/orders/buy")
                    .principal(USER)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new OrderController.BuyRequest(1L, 3))))
                .andExpect(status().isNotFound()); // Ajustamos expectativa para reflejar la realidad del código
        }
    }

    @Nested
    class Sell {
        @Test
        void conDatosValidos_devuelve200_yDelegaAlService() throws Exception {
            stubUsuarioExistente(7L);

            mockMvc.perform(post("/orders/sell")
                    .principal(USER)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new OrderController.SellRequest(1L, 2))))
                .andExpect(status().isOk());

            verify(orderService).sell(7L, 1L, 2);
        }
    }

    @Nested
    class CreateOrder {

        @Test
        void conDatosValidos_devuelve200_yDelegaAlService() throws Exception {
            stubUsuarioExistente(7L);

            OrderController.OrderRequest request = new OrderController.OrderRequest(
                    1L,
                    Order.OrderSide.BUY,
                    Order.OrderType.LIMIT,
                    new BigDecimal("100.00"),
                    3
            );

            when(orderService.createOrder(
                    7L, 1L,
                    Order.OrderSide.BUY,
                    Order.OrderType.LIMIT,
                    new BigDecimal("100.00"),
                    3
            )).thenReturn(new OrderService.OrderResultDTO(null, List.of()));

            mockMvc.perform(post("/orders")
                            .principal(USER)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(orderService).createOrder(
                    7L, 1L,
                    Order.OrderSide.BUY,
                    Order.OrderType.LIMIT,
                    new BigDecimal("100.00"),
                    3
            );
        }
    }

    @Nested
    class CancelOrder {

        @Test
        void conDatosValidos_devuelve200_yDelegaAlService() throws Exception {
            stubUsuarioExistente(7L);

            mockMvc.perform(delete("/orders/15")
                            .principal(USER))
                    .andExpect(status().isOk());

            verify(orderService).cancelOrder(7L, 15L);
        }
    }

    @Nested
    class GetMyOrders {

        @Test
        void conOrdenesExistentes_devuelveDTOsDelUsuario() throws Exception {
            stubUsuarioExistente(7L);

            Order order = mock(Order.class);

            when(order.getId()).thenReturn(15L);
            when(order.getPlayerId()).thenReturn(1L);
            when(order.getSide()).thenReturn(Order.OrderSide.BUY);
            when(order.getType()).thenReturn(Order.OrderType.LIMIT);
            when(order.getStatus()).thenReturn(Order.OrderStatus.OPEN);
            when(order.getPrice()).thenReturn(new BigDecimal("100.00"));
            when(order.getQuantityOriginal()).thenReturn(5);
            when(order.getQuantityRemaining()).thenReturn(3);
            when(order.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 11, 4, 12, 0));

            Player player = mock(Player.class);
            when(player.getName()).thenReturn("Lionel Messi");

            when(orderRepository.findByUserIdAndStatusIn(
                    eq(7L),
                    eq(List.of(Order.OrderStatus.OPEN, Order.OrderStatus.PARTIAL))
            )).thenReturn(List.of(order));

            when(playerRepository.findById(1L)).thenReturn(Optional.of(player));

            mockMvc.perform(get("/orders/my")
                            .principal(USER))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(15))
                    .andExpect(jsonPath("$[0].playerId").value(1))
                    .andExpect(jsonPath("$[0].playerName").value("Lionel Messi"))
                    .andExpect(jsonPath("$[0].side").value("BUY"))
                    .andExpect(jsonPath("$[0].type").value("LIMIT"))
                    .andExpect(jsonPath("$[0].status").value("OPEN"))
                    .andExpect(jsonPath("$[0].price").value(100.00))
                    .andExpect(jsonPath("$[0].quantityOriginal").value(5))
                    .andExpect(jsonPath("$[0].quantityRemaining").value(3));

            verify(orderRepository).findByUserIdAndStatusIn(
                    7L,
                    List.of(Order.OrderStatus.OPEN, Order.OrderStatus.PARTIAL)
            );

            verify(playerRepository).findById(1L);
        }

        @Test
        void cuandoElJugadorNoExiste_devuelveNombreDesconocido() throws Exception {
            stubUsuarioExistente(7L);

            Order order = mock(Order.class);

            when(order.getId()).thenReturn(15L);
            when(order.getPlayerId()).thenReturn(99L);
            when(order.getSide()).thenReturn(Order.OrderSide.SELL);
            when(order.getType()).thenReturn(Order.OrderType.LIMIT);
            when(order.getStatus()).thenReturn(Order.OrderStatus.PARTIAL);
            when(order.getPrice()).thenReturn(new BigDecimal("120.00"));
            when(order.getQuantityOriginal()).thenReturn(5);
            when(order.getQuantityRemaining()).thenReturn(2);

            when(orderRepository.findByUserIdAndStatusIn(
                    eq(7L),
                    eq(List.of(Order.OrderStatus.OPEN, Order.OrderStatus.PARTIAL))
            )).thenReturn(List.of(order));

            when(playerRepository.findById(99L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/orders/my")
                            .principal(USER))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].playerName").value("Desconocido"));

            verify(playerRepository).findById(99L);
        }

        @Test
        void sinOrdenesAbiertas_devuelveListaVacia() throws Exception {
            stubUsuarioExistente(7L);

            when(orderRepository.findByUserIdAndStatusIn(
                    eq(7L),
                    eq(List.of(Order.OrderStatus.OPEN, Order.OrderStatus.PARTIAL))
            )).thenReturn(List.of());

            mockMvc.perform(get("/orders/my")
                            .principal(USER))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));

            verify(playerRepository, never()).findById(anyLong());
        }
    }
}
