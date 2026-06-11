package com.ssv.watchlist.dto;

import java.util.UUID;

public record WatchlistResponse(UUID id, UUID companyId, String cik) {
}
