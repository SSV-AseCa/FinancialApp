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
		String cik = normalizeCik(request.cik());
		Company company = findCompany(cik);
		ensureNotAlreadyWatched(investorId, company);
		return saveEntry(investorId, company);
	}
	private String normalizeCik(String cik) {
		try {
			return "%010d".formatted(Long.parseLong(cik.strip()));
		} catch (NumberFormatException exception) {
			throw new IllegalArgumentException("Invalid CIK");
		}
	}

	private Company findCompany(String cik) {
		return companyStore.findByCik(cik).orElseThrow(() -> new IllegalArgumentException("Unknown CIK"));
	}

	private void ensureNotAlreadyWatched(UUID investorId, Company company) {
		if (watchlistRepository.existsByInvestorIdAndCompanyId(investorId, company.getId())) {
			throw new DuplicateWatchlistEntryException("Company already on watchlist");
		}
	}

	private WatchlistResponse saveEntry(UUID investorId, Company company) {
		try {
			WatchlistEntry saved = watchlistRepository.save(newEntry(investorId, company));
			return toResponse(saved, company);
		} catch (DataIntegrityViolationException exception) {
			throw new DuplicateWatchlistEntryException("Company already on watchlist");
		}
	}

	private WatchlistEntry newEntry(UUID investorId, Company company) {
		WatchlistEntry entry = new WatchlistEntry();
		entry.setInvestorId(investorId);
		entry.setCompanyId(company.getId());
		return entry;
	}

	private WatchlistResponse toResponse(WatchlistEntry entry, Company company) {
		return new WatchlistResponse(entry.getId(), entry.getCompanyId(), company.getCik());
	}
}
