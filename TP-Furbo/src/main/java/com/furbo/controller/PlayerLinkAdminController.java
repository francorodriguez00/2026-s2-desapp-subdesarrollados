package com.furbo.controller;

import com.furbo.model.PlayerMatchReview;
import com.furbo.repository.PlayerMatchReviewRepository;
import com.furbo.service.matching.PlayerLinkService;
import com.furbo.service.matching.PlayerMatchingPersister;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/player-links")
public class PlayerLinkAdminController {

    private final PlayerMatchReviewRepository reviewRepository;
    private final PlayerMatchingPersister persister;
    private final PlayerLinkService playerLinkService;

    public PlayerLinkAdminController(PlayerMatchReviewRepository reviewRepository, 
                                     PlayerMatchingPersister persister,
                                     PlayerLinkService playerLinkService) {
        this.reviewRepository = reviewRepository;
        this.persister = persister;
        this.playerLinkService = playerLinkService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Resumen de vínculos de jugadores", description = "Retorna contadores de jugadores totales, vinculados, no vinculados, y totales de WhoScored.")
    public ResponseEntity<com.furbo.adapter.dto.PlayerLinkSummaryDTO> summary() {
        return ResponseEntity.ok(playerLinkService.getSummary());
    }

    @GetMapping("/unmatched")
    @Operation(summary = "Jugadores de WhoScored sin vincular paginados", description = "Retorna lista paginada de jugadores de WhoScored sin player_id, ordenados por minutos totales descendente.")
    public ResponseEntity<com.furbo.adapter.dto.PagedUnmatchedDTO> unmatched(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(playerLinkService.getUnmatched(page, size));
    }

    @PostMapping("/relink")
    @Operation(summary = "Relink all players")
    public ResponseEntity<?> relink() {
        return ResponseEntity.ok(playerLinkService.relinkAll());
    }

    @PostMapping("/reviews/{externalId}/approve")
    @Operation(summary = "Aprobar match manual")
    public ResponseEntity<?> approve(@PathVariable String externalId, @RequestBody Map<String, Long> body) {
        persister.link(externalId, body.get("candidatePlayerId"), "MANUAL", 1.0);
        return ResponseEntity.ok().build();
    }
}
