package com.ssv.watchlist.application;

import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssv.company.application.CompanyStore;
import com.ssv.company.domain.Company;
import com.ssv.watchlist.domain.WatchlistEntry;
import com.ssv.watchlist.dto.AddWatchlistRequest;
import com.ssv.watchlist.dto.WatchlistResponse;
import com.ssv.watchlist.exceptions.DuplicateWatchlistEntryException;
import com.ssv.watchlist.infrastructure.persistence.WatchlistRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final CompanyStore companyStore;

    @Transactional
    public WatchlistResponse addToWatchlist(UUID investorId, AddWatchlistRequest request) {
        String cikNormalized;
        try {
            cikNormalized = "%010d".formatted(Long.parseLong(request.cik().strip()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid CIK");
        }

        Company company = companyStore.findByCik(cikNormalized)
                .orElseThrow(() -> new IllegalArgumentException("Unknown CIK"));

        if (watchlistRepository.existsByInvestorIdAndCompanyId(investorId, company.getId())) {
            throw new DuplicateWatchlistEntryException("Company already on watchlist");
        }

        WatchlistEntry entry = new WatchlistEntry();
        entry.setInvestorId(investorId);
        entry.setCompanyId(company.getId());

        try {
            WatchlistEntry saved = watchlistRepository.save(entry);
            return new WatchlistResponse(saved.getId(), saved.getCompanyId(), company.getCik());
        } catch (DataIntegrityViolationException e) {
            // Unique constraint race
            throw new DuplicateWatchlistEntryException("Company already on watchlist");
        }
    }
}
