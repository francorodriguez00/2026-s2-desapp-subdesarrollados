package com.furbo.unit.service;

import com.furbo.model.Player;
import com.furbo.repository.PlayerRepository;
import com.furbo.service.PlayerMatcherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerMatcherServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PlayerMatcherService playerMatcherService;

    private Player player(String name) {
        Player player = new Player();
        player.setName(name);
        return player;
    }

    @BeforeEach
    void setUp() {
        // No necesitamos configuración adicional.
    }

    @Test
    void returnsPlayerWhenThereIsOneExactMatch() {
        Player messi = player("Lionel Messi");

        when(playerRepository.findAll()).thenReturn(List.of(messi));

        Optional<Player> result = playerMatcherService.findMatch("Lionel Messi", "Inter Miami");

        assertTrue(result.isPresent());
        assertSame(messi, result.get());

        verify(playerRepository).findAll();
    }

    @Test
    void matchesIgnoringUppercaseAndLowercase() {
        Player messi = player("Lionel Messi");

        when(playerRepository.findAll()).thenReturn(List.of(messi));

        Optional<Player> result = playerMatcherService.findMatch("LIONEL MESSI", null);

        assertTrue(result.isPresent());
        assertSame(messi, result.get());
    }

    @Test
    void matchesIgnoringSpacesAndSpecialCharacters() {
        Player player = player("Nicolás González");

        when(playerRepository.findAll()).thenReturn(List.of(player));

        Optional<Player> result = playerMatcherService.findMatch("Nicolás-González", null);

        assertTrue(result.isPresent());
        assertSame(player, result.get());
    }

    @Test
    void returnsEmptyWhenThereIsNoMatch() {
        when(playerRepository.findAll()).thenReturn(
                List.of(player("Lionel Messi"), player("Cristiano Ronaldo"))
        );

        Optional<Player> result = playerMatcherService.findMatch("Neymar", null);

        assertTrue(result.isEmpty());
    }

    @Test
    void returnsEmptyWhenThereAreMultipleMatches() {
        Player player1 = player("Lionel Messi");
        Player player2 = player("Lionel-Messi");

        when(playerRepository.findAll()).thenReturn(List.of(player1, player2));

        Optional<Player> result = playerMatcherService.findMatch("Lionel Messi", null);

        assertTrue(result.isEmpty());
    }

    @Test
    void returnsEmptyWhenRepositoryIsEmpty() {
        when(playerRepository.findAll()).thenReturn(List.of());

        Optional<Player> result = playerMatcherService.findMatch("Lionel Messi", null);

        assertTrue(result.isEmpty());
    }

    @Test
    void returnsEmptyWhenSearchingWithNullName() {
        when(playerRepository.findAll()).thenReturn(
                List.of(player("Lionel Messi"), player("Cristiano Ronaldo"))
        );

        Optional<Player> result = playerMatcherService.findMatch(null, null);

        assertTrue(result.isEmpty());
    }

    @Test
    void matchesNullNameWithPlayerWhoseNameIsNull() {
        Player player = player(null);

        when(playerRepository.findAll()).thenReturn(List.of(player));

        Optional<Player> result = playerMatcherService.findMatch(null, null);

        assertTrue(result.isPresent());
        assertSame(player, result.get());
    }

    @Test
    void ignoresTeamWhenSearching() {
        Player messi = player("Lionel Messi");

        when(playerRepository.findAll()).thenReturn(List.of(messi));

        Optional<Player> result = playerMatcherService.findMatch("Lionel Messi", "Another Team");

        assertTrue(result.isPresent());
        assertSame(messi, result.get());
    }
}