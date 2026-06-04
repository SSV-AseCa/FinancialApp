package com.ssv.company.application;

import com.ssv.company.domain.Company;

public record CompanyFinancialData(Company company, boolean refreshed) {
}
