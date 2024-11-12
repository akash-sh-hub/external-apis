package com.example.externalApi.controller;

import com.example.externalApi.model.PrepareMessage;
import com.example.externalApi.service.FxRateService;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/fx")
public class FxRateController {

    private final FxRateService fxRateService;

    @Autowired
    PrepareMessage prepareMessage;
    @Autowired
    public FxRateController(FxRateService fxRateService) {
        this.fxRateService = fxRateService;
    }

    @GetMapping
    public @ResponseBody ResponseEntity<String> getAllFxRates() {
        JSONArray obj= fxRateService.getRatesForCurrency("EUR");
        return prepareMessage.getMessage(String.valueOf(obj));
    }

    @GetMapping("/{targetCurrency}")
    public @ResponseBody ResponseEntity<String> getFxRatesForCurrency(@PathVariable String targetCurrency) {
        JSONArray obj= fxRateService.getLatestRates(targetCurrency);
        return prepareMessage.getMessage(String.valueOf(obj));

    }

}

