package com.ssv.watchlist.application;

import com.ssv.company.application.CompanyStore;
import com.ssv.watchlist.domain.WatchlistEntry;
import com.ssv.watchlist.dto.WatchlistCompanyResponse;
import com.ssv.watchlist.infrastructure.persistence.WatchlistRepository;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WatchlistQueryService {

    private final WatchlistRepository watchlistRepository;
    private final CompanyStore companyStore;
    private final WatchlistCompanyMapper companyMapper;

    @Transactional(readOnly = true)
    public List<WatchlistCompanyResponse> getWatchlist(UUID investorId) {
        List<WatchlistEntry> entries = watchlistRepository.findByInvestorId(investorId);
        if (entries.isEmpty()) {
            return List.of();
        }
        return entries.stream()
                .map(this::mapEntry)
                .filter(Objects::nonNull)
                .toList();
    }

    private WatchlistCompanyResponse mapEntry(WatchlistEntry entry) {
        return companyStore.findById(entry.getCompanyId())
                .map(companyMapper::toResponse)
                .orElse(null);
    }
}