package com.example.externalApi.service;

import com.example.externalApi.model.FxRate;
import com.example.externalApi.repository.FxRateRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


import java.util.ArrayList;
import java.util.HashMap;

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

    public JSONArray getRatesForCurrency(String targetCurrency) {
        List<FxRate> rates = fxRateRepository.findBySourceCurrencyAndTargetCurrencyOrderByDateDesc("USD", targetCurrency);
        if (rates.isEmpty()) {
            fetchAndSaveLatestRates();
            rates = fxRateRepository.findBySourceCurrencyAndTargetCurrencyOrderByDateDesc("USD", targetCurrency);
        }
        return convertToJson(rates);
    }

    public JSONArray getLatestRates(String targetCurrency) {
        List<FxRate> rates =  fxRateRepository.findBySourceCurrencyAndTargetCurrencyOrderByDateDesc("USD", targetCurrency);
        if (rates.size() > 3) {
            rates.subList(0, 3);
        }
        return convertToJson(rates);
    }
    public void fetchAndSaveLatestRates()
    {
        String apiUrl = "https://api.frankfurter.app/latest?from=USD";
        try {
            var response = restTemplate.getForObject(apiUrl, Map.class);

        if (response != null && response.containsKey("rates")) {
            Map<String, Object> rates = (Map<String, Object>) response.get("rates");
            LocalDate date = LocalDate.parse((String) response.get("date"));
            String sourceCurrency= (String) response.get("base");
                rates.forEach((currency, rate) -> {
                    FxRate fxRate = new FxRate();
                    fxRate.setDate(date);
                    fxRate.setSourceCurrency(sourceCurrency);
                    fxRate.setTargetCurrency(currency);
                    fxRate.setRate(String.valueOf(rate));
                    fxRateRepository.save(fxRate);
                });
            }


        } catch (HttpClientErrorException | HttpServerErrorException e) {
            System.err.println("Error response from API: " + e.getStatusCode());
        } catch (RestClientException e) {
            System.err.println("Error with the API request: " + e.getMessage());
        }
            catch(Exception e)
            {
                e.printStackTrace();
            }


    }
    public JSONArray convertToJson(List<FxRate> list) {

        JSONArray jsonArray = new JSONArray(list);

        Map<String, Map<String, List<JSONObject>>> groupedData = new HashMap<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            String date = obj.getString("date");
            String sourceCurrency = obj.getString("sourceCurrency");
            String targetCurrency = obj.getString("targetCurrency");
            String rate = obj.getString("rate");

            groupedData.putIfAbsent(date, new HashMap<>());
            Map<String, List<JSONObject>> sourceCurrencyMap = groupedData.get(date);
            sourceCurrencyMap.putIfAbsent(sourceCurrency, new ArrayList<>());

            JSONObject rateEntry = new JSONObject();
            rateEntry.put("targetCurrency", targetCurrency);
            rateEntry.put("rate", rate);

            sourceCurrencyMap.get(sourceCurrency).add(rateEntry);
        }

        JSONArray resultArray = new JSONArray();

        for (String date : groupedData.keySet()) {
            Map<String, List<JSONObject>> sourceCurrencyMap = groupedData.get(date);
            for (String sourceCurrency : sourceCurrencyMap.keySet()) {
                JSONObject resultObj = new JSONObject();
                resultObj.put("date", date);
                resultObj.put("sourceCurrency", sourceCurrency);
                resultObj.put("rate", sourceCurrencyMap.get(sourceCurrency));

                resultArray.put(resultObj);
            }
        }
    return resultArray;
    }
}