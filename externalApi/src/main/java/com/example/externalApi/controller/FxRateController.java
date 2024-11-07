package com.example.externalApi.controller;

import com.example.externalApi.model.FxRate;
import com.example.externalApi.service.FxRateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RestController
@RequestMapping("/fx")
public class FxRateController {

    private final FxRateService fxRateService;

    @Autowired
    public FxRateController(FxRateService fxRateService) {
        this.fxRateService = fxRateService;
    }

    @GetMapping
    public List<FxRate> getAllFxRates() {
        return fxRateService.getRatesForCurrency("EUR"); // Default target currency
    }

    @GetMapping("/{targetCurrency}")
    public List<FxRate> getFxRatesForCurrency(@PathVariable String targetCurrency) {
        return fxRateService.getRatesForCurrency(targetCurrency);
    }
}

