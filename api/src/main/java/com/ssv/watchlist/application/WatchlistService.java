package com.ssv.watchlist.application;

import com.ssv.company.application.CompanyResearchService;
import com.ssv.company.application.CompanyStore;
import com.ssv.company.domain.CikUtils;
import com.ssv.company.domain.Company;
import com.ssv.watchlist.domain.WatchlistEntry;
import com.ssv.watchlist.dto.AddWatchlistRequest;
import com.ssv.watchlist.dto.WatchlistResponse;
import com.ssv.watchlist.exceptions.DuplicateWatchlistEntryException;
import com.ssv.watchlist.exceptions.WatchlistEntryNotFoundException;
import com.ssv.watchlist.infrastructure.persistence.WatchlistRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WatchlistService {

	private final WatchlistRepository watchlistRepository;
	private final CompanyStore companyStore;
	private final CompanyResearchService companyResearchService;

	@Transactional
	public WatchlistResponse addToWatchlist(UUID investorId, AddWatchlistRequest request) {
		String cik = CikUtils.normalize(request.cik());
		Company company = findCompany(cik);
		ensureNotAlreadyWatched(investorId, company);
		return saveEntry(investorId, company);
	}

	@Transactional
	public void removeFromWatchlist(UUID investorId, String cik) {
		Company company = findCompanyForRemoval(cik);
		WatchlistEntry entry = findEntry(investorId, company);
		watchlistRepository.delete(entry);
	}

	private Company findCompanyForRemoval(String cik) {
		String normalizedCik = CikUtils.normalize(cik);
		return companyStore.findByCik(normalizedCik)
				.orElseThrow(() -> new WatchlistEntryNotFoundException("Company not found"));
	}

	private WatchlistEntry findEntry(UUID investorId, Company company) {
		return watchlistRepository.findByInvestorIdAndCompanyId(investorId, company.getId())
				.orElseThrow(() -> new WatchlistEntryNotFoundException("Watchlist entry not found"));
	}

	private Company findCompany(String cik) {
		return companyResearchService.getOrFetchCompany(cik);
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
