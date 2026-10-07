package com.furbo.unit;

import com.furbo.controller.PlayerController;
import com.furbo.service.PlayerService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class PlayerControllerTest {

    @Mock
    private PlayerService playerService;

    @InjectMocks
    private PlayerController playerController;

    public PlayerControllerTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getPlayers_ReturnsOk() {
        when(playerService.getPlayers()).thenReturn(java.util.Collections.emptyList());
        ResponseEntity<?> response = playerController.getPlayers();
        assertEquals(200, response.getStatusCodeValue());
    }
}
