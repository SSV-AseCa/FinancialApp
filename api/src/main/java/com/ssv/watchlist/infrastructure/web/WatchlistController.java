package com.ssv.watchlist.infrastructure.web;

import com.ssv.investor.infrastructure.filter.InvestorProvisioningFilter;
import com.ssv.watchlist.application.WatchlistQueryService;
import com.ssv.watchlist.application.WatchlistService;
import com.ssv.watchlist.dto.AddWatchlistRequest;
import com.ssv.watchlist.dto.WatchlistCompanyResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/watchlist")
@RequiredArgsConstructor
public class WatchlistController {

	private final WatchlistService watchlistService;
	private final WatchlistQueryService watchlistQueryService;

	@PostMapping
	public ResponseEntity<?> addToWatchlist(HttpServletRequest request, @Valid @RequestBody AddWatchlistRequest body) {
		UUID investorId = investorIdFrom(request);
		var created = watchlistService.addToWatchlist(investorId, body);
		return ResponseEntity.created(URI.create("/watchlist/" + created.id())).body(created);
	}

	@DeleteMapping("/{cik}")
	public ResponseEntity<Void> removeFromWatchlist(HttpServletRequest request, @PathVariable String cik) {
		UUID investorId = investorIdFrom(request);
		watchlistService.removeFromWatchlist(investorId, cik);
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	public ResponseEntity<List<WatchlistCompanyResponse>> getWatchlist(HttpServletRequest request) {
		return ResponseEntity.ok(watchlistQueryService.getWatchlist(investorIdFrom(request)));
	}

	private UUID investorIdFrom(HttpServletRequest request) {
		return (UUID) request.getAttribute(InvestorProvisioningFilter.INVESTOR_ID_ATTR);
	}
}
