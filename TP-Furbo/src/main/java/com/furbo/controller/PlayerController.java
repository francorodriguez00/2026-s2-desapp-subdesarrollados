package com.furbo.controller;

import com.furbo.model.Player;
import com.furbo.service.PlayerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping
    public ResponseEntity<Page<Player>> getPlayers(
            @RequestParam(required = false) String league,
            @RequestParam(required = false) String team,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) String name,
            @PageableDefault(size = 50, sort = {"name", "id"}) Pageable pageable) {
        return ResponseEntity.ok(playerService.getPlayers(league, team, position, name, pageable));
    }
}
