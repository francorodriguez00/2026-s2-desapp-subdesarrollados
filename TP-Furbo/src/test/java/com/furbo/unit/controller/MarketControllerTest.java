
package com.furbo.unit.controller;

import com.furbo.controller.MarketController;
import com.furbo.model.Player;
import com.furbo.model.Portfolio;
import com.furbo.model.PosicionPorJugador;
import com.furbo.model.SellOffer;
import com.furbo.model.User;
import com.furbo.repository.PlayerRepository;
import com.furbo.repository.PortfolioRepository;
import com.furbo.repository.PosicionPorJugadorRepository;
import com.furbo.repository.SellOfferRepository;
import com.furbo.repository.UserRepository;
import com.furbo.security.JwtAuthenticationFilter;
import com.furbo.service.OrderService;
import com.furbo.service.PriceProvider;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = MarketController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
public class MarketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private PriceProvider priceProvider;
    @MockBean private OrderService orderService;
    @MockBean private SellOfferRepository sellOfferRepository;
    @MockBean private UserRepository userRepository;
    @MockBean private PlayerRepository playerRepository;
    @MockBean private PosicionPorJugadorRepository posicionRepository;
    @MockBean private PortfolioRepository portfolioRepository;

    private static final Principal USER = () -> "user@furbo.com";

    private void stubUsuarioExistente(Long id) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);

        when(userRepository.findByEmail("user@furbo.com"))
                .thenReturn(Optional.of(user));
    }

    @Nested
    class GetPlayerPrice {

        @Test
        void conJugadorExistente_devuelvePrecioActual() throws Exception {
            BigDecimal expectedPrice = new BigDecimal("125.50");

            when(priceProvider.getCurrentPrice(1L))
                    .thenReturn(expectedPrice);

            mockMvc.perform(get("/market/price/1"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(content().string("125.50"));

            verify(priceProvider).getCurrentPrice(1L);
        }
    }

    @Nested
    class GetBook {

        @Test
        void conJugadorExistente_devuelveLibroDeOrdenes() throws Exception {
            OrderService.BookResponseDTO bookResponse = new OrderService.BookResponseDTO(
                    List.of(),
                    List.of(),
                    new BigDecimal("150.00")
            );

            when(orderService.getOrderBook(1L)).thenReturn(bookResponse);

            mockMvc.perform(get("/market/book/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.buyLevels", hasSize(0)))
                    .andExpect(jsonPath("$.sellLevels", hasSize(0)))
                    .andExpect(jsonPath("$.lastPrice").value(150.00));

            verify(orderService).getOrderBook(1L);
        }
    }

    @Nested
    class GetLastTrade {

        @Test
        void conOperacionesPrevias_devuelvePrecioUltimaOperacion() throws Exception {
            BigDecimal lastTrade = new BigDecimal("150.75");

            when(orderService.getLastTrade(1L)).thenReturn(lastTrade);

            mockMvc.perform(get("/market/last-trade/1"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("150.75"));

            verify(orderService).getLastTrade(1L);
            verify(priceProvider, never()).getCurrentPrice(anyLong());
        }

        @Test
        void sinOperacionesPrevias_devuelvePrecioActualDelJugador() throws Exception {
            BigDecimal currentPrice = new BigDecimal("120.00");

            when(orderService.getLastTrade(1L)).thenReturn(null);
            when(priceProvider.getCurrentPrice(1L)).thenReturn(currentPrice);

            mockMvc.perform(get("/market/last-trade/1"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("120.00"));

            verify(orderService).getLastTrade(1L);
            verify(priceProvider).getCurrentPrice(1L);
        }
    }

    @Nested
    class GetMyOffers {

        @Test
        void devuelveSolamenteOfertasAbiertasDelUsuarioAutenticado() throws Exception {
            stubUsuarioExistente(7L);

            SellOffer ownOpenOffer = mock(SellOffer.class);
            when(ownOpenOffer.getId()).thenReturn(1L);
            when(ownOpenOffer.getSellerId()).thenReturn(7L);
            when(ownOpenOffer.getPlayerId()).thenReturn(10L);
            when(ownOpenOffer.getQuantityRemaining()).thenReturn(5);
            when(ownOpenOffer.getStatus()).thenReturn(SellOffer.OfferStatus.OPEN);

            SellOffer ownClosedOffer = mock(SellOffer.class);
            when(ownClosedOffer.getSellerId()).thenReturn(7L);
            when(ownClosedOffer.getStatus()).thenReturn(SellOffer.OfferStatus.FILLED);

            SellOffer otherUserOffer = mock(SellOffer.class);
            when(otherUserOffer.getSellerId()).thenReturn(8L);
            when(otherUserOffer.getStatus()).thenReturn(SellOffer.OfferStatus.OPEN);

            Player player = mock(Player.class);
            when(player.getName()).thenReturn("Lionel Messi");

            when(sellOfferRepository.findAll())
                    .thenReturn(List.of(ownOpenOffer, ownClosedOffer, otherUserOffer));

            when(playerRepository.findById(10L))
                    .thenReturn(Optional.of(player));

            when(priceProvider.getCurrentPrice(10L))
                    .thenReturn(new BigDecimal("200.00"));

            mockMvc.perform(get("/market/my-offers")
                            .principal(USER))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(sellOfferRepository).findAll();
            verify(playerRepository).findById(10L);
            verify(priceProvider).getCurrentPrice(10L);
        }

        @Test
        void cuandoElJugadorNoExiste_usaNombreDesconocido() throws Exception {
            stubUsuarioExistente(7L);

            SellOffer offer = mock(SellOffer.class);
            when(offer.getSellerId()).thenReturn(7L);
            when(offer.getPlayerId()).thenReturn(99L);
            when(offer.getQuantityRemaining()).thenReturn(2);
            when(offer.getStatus()).thenReturn(SellOffer.OfferStatus.OPEN);

            when(sellOfferRepository.findAll()).thenReturn(List.of(offer));
            when(playerRepository.findById(99L)).thenReturn(Optional.empty());
            when(priceProvider.getCurrentPrice(99L))
                    .thenReturn(new BigDecimal("50.00"));

            mockMvc.perform(get("/market/my-offers")
                            .principal(USER))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(playerRepository).findById(99L);
            verify(priceProvider).getCurrentPrice(99L);
        }
    }

    @Nested
    class CancelOffer {

        @Test
        void siendoPropietario_cancelaOferta_yLiberaCantidadReservada() throws Exception {
            stubUsuarioExistente(7L);

            SellOffer offer = mock(SellOffer.class);
            when(offer.getSellerId()).thenReturn(7L);
            when(offer.getPlayerId()).thenReturn(10L);
            when(offer.getQuantityRemaining()).thenReturn(4);

            User seller = mock(User.class);
            when(seller.getId()).thenReturn(7L);

            Portfolio portfolio = mock(Portfolio.class);
            when(portfolio.getId()).thenReturn(3L);

            PosicionPorJugador posicion = mock(PosicionPorJugador.class);
            when(posicion.getReservedQuantity()).thenReturn(10);

            when(sellOfferRepository.findById(1L))
                    .thenReturn(Optional.of(offer));

            when(userRepository.findById(7L))
                    .thenReturn(Optional.of(seller));

            when(portfolioRepository.findByUser(seller))
                    .thenReturn(Optional.of(portfolio));

            when(posicionRepository.findByPortfolioIdAndPlayerId(3L, 10L))
                    .thenReturn(Optional.of(posicion));

            mockMvc.perform(delete("/market/offers/1")
                            .principal(USER))
                    .andExpect(status().isOk());

            verify(posicion).setReservedQuantity(6);
            verify(posicionRepository).save(posicion);
            verify(sellOfferRepository).delete(offer);
        }

        @Test
        void siendoOtroUsuario_devuelve403_yNoCancelaLaOferta() throws Exception {
            stubUsuarioExistente(7L);

            SellOffer offer = mock(SellOffer.class);
            when(offer.getSellerId()).thenReturn(8L);

            when(sellOfferRepository.findById(1L))
                    .thenReturn(Optional.of(offer));

            mockMvc.perform(delete("/market/offers/1")
                            .principal(USER))
                    .andExpect(status().isForbidden());

            verify(sellOfferRepository, never()).delete(any(SellOffer.class));
            verify(userRepository, never()).findById(anyLong());
            verify(posicionRepository, never()).save(any());
        }
    }
}
