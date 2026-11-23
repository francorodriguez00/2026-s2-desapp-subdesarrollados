package com.furbo.controller;

import com.furbo.adapter.dto.*;
import com.furbo.service.matching.PlayerLinkAdminService;
import com.furbo.service.matching.PlayerLinkService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/player-links")
public class PlayerLinkAdminController {

    private final PlayerLinkService playerLinkService;
    private final PlayerLinkAdminService adminService;

    public PlayerLinkAdminController(PlayerLinkService playerLinkService, PlayerLinkAdminService adminService) {
        this.playerLinkService = playerLinkService;
        this.adminService = adminService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Resumen de vínculos de jugadores", description = "Retorna contadores de jugadores totales, vinculados, no vinculados, y totales de WhoScored.")
    public ResponseEntity<PlayerLinkSummaryDTO> summary() {
        return ResponseEntity.ok(playerLinkService.getSummary());
    }

    @GetMapping("/unmatched")
    @Operation(summary = "Jugadores de WhoScored sin vincular paginados", description = "Retorna lista paginada de jugadores de WhoScored sin player_id, ordenados por minutos totales descendente.")
    public ResponseEntity<PagedUnmatchedDTO> unmatched(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(playerLinkService.getUnmatched(page, size));
    }

    @GetMapping("/reviews")
    @Operation(summary = "Revisiones pendientes agrupadas", description = "Retorna lista paginada de revisiones pendientes agrupadas por jugador de WhoScored.")
    public ResponseEntity<PagedReviewItemsDTO> reviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getPendingReviews(page, size));
    }

    @GetMapping("/unmatched/{whoscoredId}/candidates")
    @Operation(summary = "Candidatos para jugador WhoScored sin vincular", description = "Retorna el resumen de WhoScored y candidatos potenciales para un jugador sin vincular.")
    public ResponseEntity<UnmatchedCandidatesResponseDTO> unmatchedCandidates(
            @PathVariable Long whoscoredId,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(adminService.getUnmatchedCandidates(whoscoredId, name, size));
    }

    @GetMapping("/links/to-confirm")
    @Operation(summary = "Vínculos para confirmar", description = "Retorna lista paginada de vínculos marcados con needsReview = true.")
    public ResponseEntity<PagedToConfirmDTO> toConfirm(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getToConfirmLinks(page, size));
    }

    @PostMapping("/relink")
    @Operation(summary = "Relink all players", description = "Recalcula todos los vínculos de WhoScored.")
    public ResponseEntity<?> relink() {
        return ResponseEntity.ok(playerLinkService.relinkAll());
    }

    @PostMapping("/reviews/{externalId}/approve")
    @Operation(summary = "Aprobar match candidato", description = "Aprueba un candidato de revisión pendiente y vincula el jugador manualmente.")
    public ResponseEntity<LinkActionResultDTO> approve(
            @PathVariable String externalId,
            @RequestBody ApproveRequestDTO body) {
        return ResponseEntity.ok(adminService.approve(externalId, body != null ? body.candidatePlayerId() : null));
    }

    @PostMapping("/reviews/{externalId}/reject")
    @Operation(summary = "Rechazar match candidato", description = "Rechaza un candidato pendiente (o todos si no se indica candidato).")
    public ResponseEntity<LinkActionResultDTO> reject(
            @PathVariable String externalId,
            @RequestBody(required = false) RejectRequestDTO body) {
        Long candidateId = (body != null) ? body.candidatePlayerId() : null;
        return ResponseEntity.ok(adminService.reject(externalId, candidateId));
    }

    @PostMapping("/links/{externalId}")
    @Operation(summary = "Vincular jugador manualmente", description = "Vincula un externalId de WhoScored a un player_id de forma manual.")
    public ResponseEntity<LinkActionResultDTO> linkManually(
            @PathVariable String externalId,
            @RequestBody LinkManualRequestDTO body) {
        return ResponseEntity.ok(adminService.linkManually(externalId, body.playerId()));
    }

    @DeleteMapping("/links/{externalId}")
    @Operation(summary = "Desvincular jugador", description = "Elimina el vínculo de un externalId y registra rechazo para evitar re-vinculación automática.")
    public ResponseEntity<LinkActionResultDTO> unlink(@PathVariable String externalId) {
        return ResponseEntity.ok(adminService.unlink(externalId));
    }

    @PostMapping("/links/{externalId}/confirm")
    @Operation(summary = "Confirmar vínculo", description = "Marca needsReview como falso para el vínculo existente.")
    public ResponseEntity<LinkActionResultDTO> confirm(@PathVariable String externalId) {
        return ResponseEntity.ok(adminService.confirm(externalId));
    }
}
