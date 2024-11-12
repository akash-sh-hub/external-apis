// src/main/java/com/example/fxservice/repository/FxRateRepository.java
package com.example.externalApi.repository;

import com.example.externalApi.model.FxRate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface FxRateRepository extends JpaRepository<FxRate, Long> {
    List<FxRate> findBySourceCurrencyAndTargetCurrencyOrderByDateDesc(String sourceCurrency, String targetCurrency);
}