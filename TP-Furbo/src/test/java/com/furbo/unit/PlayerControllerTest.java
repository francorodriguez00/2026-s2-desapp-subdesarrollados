package com.furbo.unit;

import com.furbo.controller.PlayerController;
import com.furbo.model.Player;
import com.furbo.service.PlayerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerControllerTest {

    @Mock
    private PlayerService playerService;

    @InjectMocks
    private PlayerController playerController;

    @Test
    void getPlayers_ReturnsOk() {
        when(playerService.getPlayers(any(), any(), any(), any())).thenReturn(Page.empty());
        ResponseEntity<?> response = playerController.getPlayers(null, null, null, Pageable.unpaged());
        assertEquals(200, response.getStatusCodeValue());
    }
}
