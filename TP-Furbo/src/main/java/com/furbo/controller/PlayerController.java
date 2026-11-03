package com.furbo.controller;

import com.furbo.adapter.dto.PlayerCatalogDTO;
import com.furbo.repository.PlayerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/players")
public class PlayerController {

    private final PlayerRepository playerRepository;

    public PlayerController(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }


    @GetMapping
    public ResponseEntity<Page<PlayerCatalogDTO>> getPlayers(
            @RequestParam(required = false) String league,
            @RequestParam(required = false) String team,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(playerRepository.findPlayersCatalog(
                league == null ? "" : league,
                position == null ? "" : position,
                name == null ? "" : name,
                date == null ? LocalDateTime.now() : date,
                PageRequest.of(page, size)));
    }
}
