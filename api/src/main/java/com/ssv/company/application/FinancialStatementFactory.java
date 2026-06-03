package com.ssv.company.application;

import com.ssv.company.domain.Company;
import com.ssv.company.domain.FinancialStatement;
import com.ssv.company.domain.FinancialStatementCreateRequest;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class FinancialStatementFactory {

    public FinancialStatement create(Company company, EdgarFinancialMetric metric, Instant fetchedAt) {
        FinancialStatementCreateRequest request = new FinancialStatementCreateRequest(company, metric.metric(),
                metric.value(), metric.unit(), metric.periodEnd(), fetchedAt);
        return new FinancialStatement(request);
    }
}