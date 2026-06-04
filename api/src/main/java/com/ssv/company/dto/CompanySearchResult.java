package com.ssv.company.dto;

import java.util.List;

public record CompanySearchResult(String name, String cik, List<String> tickers) {
}
