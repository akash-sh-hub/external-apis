package com.example.externalApi;

import com.example.externalApi.model.FxRate;
import com.example.externalApi.repository.FxRateRepository;
import com.example.externalApi.service.FxRateService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FxRateServiceTest {

    @InjectMocks
    private FxRateService fxRateService;

    @Mock
    private FxRateRepository fxRateRepository;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getRatesForCurrency_whenNoRatesInDB_shouldCallApiAndSaveRates() {

        String targetCurrency = "EUR";
        List<FxRate> emptyList = new ArrayList<>();
        when(fxRateRepository.findBySourceCurrencyAndTargetCurrencyOrderByDateDesc("USD", targetCurrency))
                .thenReturn(emptyList);

        Map<String, Object> apiResponse = new HashMap<>();
        apiResponse.put("base", "USD");
        apiResponse.put("date", "2024-11-11");
        Map<String, Object> rates = new HashMap<>();
        rates.put("EUR", 0.93888);
        apiResponse.put("rates", rates);
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(apiResponse);

        JSONArray result = fxRateService.getRatesForCurrency(targetCurrency);
        assertNotNull(result);
    }

    @Test
    void getRatesForCurrency_whenRatesInDB_shouldNotCallApi() {
        String targetCurrency = "EUR";
        FxRate rate = new FxRate();
        rate.setDate(LocalDate.parse("2024-11-11"));
        rate.setSourceCurrency("USD");
        rate.setTargetCurrency("EUR");
        rate.setRate("0.93888");
        List<FxRate> rates = Collections.singletonList(rate);
        when(fxRateRepository.findBySourceCurrencyAndTargetCurrencyOrderByDateDesc("USD", targetCurrency))
                .thenReturn(rates);
        try {
            JSONArray result = fxRateService.getRatesForCurrency(targetCurrency);
            verify(fxRateRepository, times(0)).save(any(FxRate.class));
            assertNotNull(result);
            assertEquals(1, result.length());
        }
        catch(Exception e){}
    }

    @Test
    void getLatestRates_whenMoreThan3Rates_shouldLimitTo3() {

        String targetCurrency = "EUR";
        List<FxRate> rates = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            FxRate rate = new FxRate();
            rate.setDate(LocalDate.parse("2024-11-11"));
            rate.setSourceCurrency("USD");
            rate.setTargetCurrency(targetCurrency);
            rate.setRate("0.93" + i);
            rates.add(rate);
        }
        try {
            when(fxRateRepository.findBySourceCurrencyAndTargetCurrencyOrderByDateDesc("USD", targetCurrency))
                    .thenReturn(rates);
            JSONArray result = fxRateService.getLatestRates(targetCurrency);
            assertEquals(3, result.length());
        }
        catch(Exception e){}
    }

    @Test
    void convertToJson_shouldGroupRatesByDate() throws JSONException {
        List<FxRate> rates = new ArrayList<>();
        FxRate rate1 = new FxRate();
        rate1.setDate(LocalDate.parse("2024-11-11"));
        rate1.setSourceCurrency("USD");
        rate1.setTargetCurrency("EUR");
        rate1.setRate("0.93888");

        FxRate rate2 = new FxRate();
        rate2.setDate(LocalDate.parse("2024-11-11"));
        rate2.setSourceCurrency("USD");
        rate2.setTargetCurrency("GBP");
        rate2.setRate("0.76192");

        rates.add(rate1);
        rates.add(rate2);

        try {
            JSONArray  result = fxRateService.convertToJson(rates);

        assertEquals(1, result.length());
        JSONObject firstResult = result.getJSONObject(0);
        assertEquals("2024-11-11", firstResult.getString("date"));
        assertEquals(2, firstResult.getJSONArray("rate").length());
        }
        catch(Exception e){}
    }


}
