package com.ssv.watchlist.infrastructure.web;

import java.net.URI;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssv.investor.infrastructure.filter.InvestorProvisioningFilter;
import com.ssv.watchlist.application.WatchlistService;
import com.ssv.watchlist.dto.AddWatchlistRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/watchlist")
@RequiredArgsConstructor
public class WatchlistController {

	private final WatchlistService watchlistService;

	@PostMapping
	public ResponseEntity<?> addToWatchlist(HttpServletRequest request, @Valid @RequestBody AddWatchlistRequest body) {
		UUID investorId = (UUID) request.getAttribute(InvestorProvisioningFilter.INVESTOR_ID_ATTR);
		var created = watchlistService.addToWatchlist(investorId, body);
		return ResponseEntity.created(URI.create("/watchlist/" + created.id())).body(created);
	}

	@DeleteMapping("/{cik}")
	public ResponseEntity<Void> removeFromWatchlist(HttpServletRequest request, @PathVariable String cik) {
		UUID investorId = (UUID) request.getAttribute(InvestorProvisioningFilter.INVESTOR_ID_ATTR);
		watchlistService.removeFromWatchlist(investorId, cik);
		return ResponseEntity.noContent().build();
	}
}
