package com.travian.interceptor.api;

import com.travian.interceptor.service.InterceptCalculatorService;
import com.travian.interceptor.service.TroopCatalogService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TravianController {

    private final TroopCatalogService troopCatalogService;
    private final InterceptCalculatorService interceptCalculatorService;

    public TravianController(
            TroopCatalogService troopCatalogService,
            InterceptCalculatorService interceptCalculatorService
    ) {
        this.troopCatalogService = troopCatalogService;
        this.interceptCalculatorService = interceptCalculatorService;
    }

    @GetMapping("/reference")
    public ReferenceDataResponse referenceData() {
        return troopCatalogService.getReferenceData();
    }

    @PostMapping("/calculate")
    public CalculationResponse calculate(@Valid @RequestBody CalculationRequest request) {
        return interceptCalculatorService.calculate(request);
    }
}
