package com.example.externalApi;

import com.example.externalApi.model.FxRate;
import com.example.externalApi.repository.FxRateRepository;
import com.example.externalApi.service.FxRateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class FxRateServiceTest {

    @Mock
    private FxRateRepository fxRateRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private FxRateService fxRateService;

    @BeforeEach
    void setUp() {
        fxRateService = new FxRateService(fxRateRepository);
    }

    @Test
    void testGetRatesForCurrency_whenRatesExistInDb() {
        String targetCurrency = "EUR";
        LocalDate today = LocalDate.now();
        FxRate fxRate = new FxRate();
        fxRate.setDate(today);
        fxRate.setSourceCurrency("USD");
        fxRate.setTargetCurrency(targetCurrency);
        fxRate.setRate(BigDecimal.valueOf(0.85));

        when(fxRateRepository.existsByDateAndTargetCurrency(today, targetCurrency)).thenReturn(true);
        when(fxRateRepository.findTop3ByTargetCurrencyOrderByDateDesc(targetCurrency))
                .thenReturn(List.of(fxRate));

        List<FxRate> rates = fxRateService.getRatesForCurrency(targetCurrency);
        verify(fxRateRepository).existsByDateAndTargetCurrency(today, targetCurrency);
        assertEquals(1, rates.size());
        assertEquals(targetCurrency, rates.get(0).getTargetCurrency());
        assertEquals(BigDecimal.valueOf(0.85), rates.get(0).getRate());
    }

    @Test
    void testGetRatesForCurrency_whenRatesDoNotExistInDb_andFetchesFromApi() {
        String targetCurrency = "EUR";
        LocalDate today = LocalDate.now();
        when(fxRateRepository.existsByDateAndTargetCurrency(today, targetCurrency)).thenReturn(false);

        Map<String, Double> ratesMap = Map.of(
                "EUR", 0.85,
                "GBP", 0.75
        );
        Map<String, Object> response = Map.of("date", today.toString(), "rates", ratesMap);
        when(restTemplate.getForObject(any(String.class), any(Class.class)))
                .thenReturn(response);

        doNothing().when(fxRateRepository).save(any(FxRate.class));

        List<FxRate> rates = fxRateService.getRatesForCurrency(targetCurrency);

        verify(fxRateRepository).existsByDateAndTargetCurrency(today, targetCurrency);
        verify(fxRateRepository, times(2)).save(any(FxRate.class));  // One for EUR, one for GBP
        assertNotNull(rates);
        assertTrue(rates.stream().anyMatch(rate -> rate.getTargetCurrency().equals("EUR")));
    }

    @Test
    void testFetchAndSaveLatestRates() {
        String apiUrl = "https://api.frankfurter.app/latest?from=USD";

        Map<String, Double> ratesMap = Map.of(
                "EUR", 0.85,
                "GBP", 0.75
        );
        Map<String, Object> response = Map.of("date", "2024-11-07", "rates", ratesMap);
        when(restTemplate.getForObject(apiUrl, Map.class)).thenReturn(response);

      try {
            fxRateService.getClass().getDeclaredMethod("fetchAndSaveLatestRates").setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        try {
            fxRateService.getClass().getDeclaredMethod("fetchAndSaveLatestRates").invoke(fxRateService);
        } catch (Exception e) {
            fail("Exception during fetch and save: " + e.getMessage());
        }

       verify(fxRateRepository, times(2)).save(any(FxRate.class));
    }

    @Test
    void testFetchAndSaveLatestRates_whenApiReturnsNull() {
       when(restTemplate.getForObject(any(String.class), any(Class.class)))
                .thenReturn(null);

       try {
            fxRateService.getClass().getDeclaredMethod("fetchAndSaveLatestRates").setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        try {
            fxRateService.getClass().getDeclaredMethod("fetchAndSaveLatestRates").invoke(fxRateService);
        } catch (Exception e) {
            fail("Exception during fetch and save: " + e.getMessage());
        }

        verify(fxRateRepository, never()).save(any(FxRate.class));
    }
}
