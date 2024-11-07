package com.example.externalApi.service;

import com.example.externalApi.model.FxRate;
import com.example.externalApi.repository.FxRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class FxRateService {

    private final FxRateRepository fxRateRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    public FxRateService(FxRateRepository fxRateRepository) {
        this.fxRateRepository = fxRateRepository;
    }

    public List<FxRate> getRatesForCurrency(String targetCurrency) {
        LocalDate today = LocalDate.now();

        if (!fxRateRepository.existsByDateAndTargetCurrency(today, targetCurrency)) {
            fetchAndSaveLatestRates();
        }

        return fxRateRepository.findTop3ByTargetCurrencyOrderByDateDesc(targetCurrency);
    }

    private void fetchAndSaveLatestRates() {
        String apiUrl = "https://api.frankfurter.app/latest?from=USD";
        var response = restTemplate.getForObject(apiUrl, Map.class);

        if (response != null && response.containsKey("rates")) {
            Map<String, Double> rates = (Map<String, Double>) response.get("rates");
            LocalDate date = LocalDate.parse((String) response.get("date"));

            rates.forEach((currency, rate) -> {
                FxRate fxRate = new FxRate();
                fxRate.setDate(date);
                fxRate.setSourceCurrency("USD");
                fxRate.setTargetCurrency(currency);
                fxRate.setRate(BigDecimal.valueOf(rate));
                fxRateRepository.save(fxRate);
            });
        }
    }
}