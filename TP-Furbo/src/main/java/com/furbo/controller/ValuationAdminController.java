package com.furbo.controller;

import com.furbo.model.StrategyConfig;
import com.furbo.model.StrategyConfigData;
import com.furbo.service.valuation.QuotationService;
import com.furbo.service.valuation.StrategyConfigValidator;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/valuation")
public class ValuationAdminController {

    private final QuotationService service;
    private final StrategyConfigValidator validator;

    public ValuationAdminController(QuotationService service, StrategyConfigValidator validator) {
        this.service = service;
        this.validator = validator;
    }

    @GetMapping("/strategies")
    @Operation(summary = "Lista todas las configuraciones")
    public ResponseEntity<List<StrategyConfig>> listStrategies() {
        return ResponseEntity.ok(service.listAllConfigs());
    }

    @PutMapping("/active")
    @Operation(summary = "Activa una estrategia")
    public ResponseEntity<?> activate(@RequestBody Map<String, Object> body) {
        String code = (String) body.get("strategyCode");
        Integer version = body.containsKey("version") ? (Integer) body.get("version") : null;
        
        if (version == null) {
            service.activateLatest(code);
        } else {
            service.activate(code, version);
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping("/strategies/{code}")
    @Operation(summary = "Crea nueva versión de estrategia")
    public ResponseEntity<?> updateStrategy(@PathVariable String code, @RequestBody StrategyConfigData data) {
        validator.validate(data);
        service.createNewVersion(code, data);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/recalculate")
    @Operation(summary = "Recalcula todo")
    public ResponseEntity<?> recalculate() {
        service.recalculateAll(com.furbo.model.QuoteTrigger.MANUAL);
        return ResponseEntity.ok().build();
    }
}
