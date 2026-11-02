package com.furbo.controller;

import com.furbo.adapter.dto.PlayerResponseDTO;
import com.furbo.model.Player;
import com.furbo.model.PlayerQuote;
import com.furbo.repository.PlayerQuoteRepository;
import com.furbo.service.PlayerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/players")
public class PlayerController {

    private final PlayerService playerService;
    private final PlayerQuoteRepository quoteRepository;

    public PlayerController(PlayerService playerService, PlayerQuoteRepository quoteRepository) {
        this.playerService = playerService;
        this.quoteRepository = quoteRepository;
    }

    @GetMapping
    public ResponseEntity<Page<PlayerResponseDTO>> getPlayers(
            @RequestParam(required = false) String league,
            @RequestParam(required = false) String team,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) String name,
            @PageableDefault(size = 50, sort = {"name", "id"}) Pageable pageable) {
        
        // Obtenemos la página original de jugadores filtrada
        Page<Player> playerPage = playerService.getPlayers(league, team, position, name, pageable);
        
        // Convertimos a DTO y asociamos cotización
        // NOTA: Para ordenamiento real por precio vía DB se requiere un JOIN complejo
        // Mantenemos la lógica actual pero asegurando el retorno correcto
        Page<PlayerResponseDTO> response = playerPage.map(p -> {
            BigDecimal price = quoteRepository.findFirstByPlayerIdOrderByCalculatedAtDescIdDesc(p.getId())
                    .map(PlayerQuote::getPrice)
                    .orElse(BigDecimal.ONE); // Valor base 1 si no hay quote
            
            return PlayerResponseDTO.builder()
                    .id(p.getId())
                    .name(p.getName())
                    .position(p.getPosition())
                    .team(p.getTeam())
                    .nationality(p.getNationality())
                    .league(p.getLeague())
                    .price(price)
                    .build();
        });
        
        // Ordenamos en memoria solo la página actual por precio descendente
        List<PlayerResponseDTO> sortedContent = response.getContent().stream()
                .sorted((a, b) -> b.getPrice().compareTo(a.getPrice()))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(new org.springframework.data.domain.PageImpl<>(
                sortedContent, pageable, response.getTotalElements()));
    }
}
