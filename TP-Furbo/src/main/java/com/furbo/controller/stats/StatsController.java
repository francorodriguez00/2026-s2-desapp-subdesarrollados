package com.furbo.controller.stats;

import com.furbo.service.stats.StatsImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stats")
public class StatsController {

    private final StatsImportService statsImportService;

    public StatsController(StatsImportService statsImportService) {
        this.statsImportService = statsImportService;
    }

    @PostMapping("/import")
    // Se asume que existe una política de autorización, ajusta según la necesidad del proyecto
    public ResponseEntity<String> importStats(
            @RequestParam String url,
            @RequestParam String seasonId,
            @RequestParam String competition) {
        
        statsImportService.importStats(url, seasonId, competition);
        return ResponseEntity.ok("Importación iniciada para " + competition);
    }
}
