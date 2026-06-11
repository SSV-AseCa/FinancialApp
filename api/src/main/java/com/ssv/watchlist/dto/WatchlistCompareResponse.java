package com.ssv.watchlist.dto;

import java.util.List;

public record WatchlistCompareResponse(List<WatchlistCompareCompanyResponse> companies) {

    public WatchlistCompareResponse {
        companies = List.copyOf(companies);
    }
}