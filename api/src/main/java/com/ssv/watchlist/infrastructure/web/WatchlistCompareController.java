package com.ssv.watchlist.infrastructure.web;

import com.ssv.investor.infrastructure.filter.InvestorProvisioningFilter;
import com.ssv.watchlist.application.WatchlistCompareService;
import com.ssv.watchlist.dto.WatchlistCompareResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/watchlist")
@RequiredArgsConstructor
public class WatchlistCompareController {

	private final WatchlistCompareService watchlistCompareService;

	@GetMapping("/compare")
	public ResponseEntity<WatchlistCompareResponse> compareWatchlist(HttpServletRequest request,
			@RequestParam String ciks) {

		UUID investorId = investorIdFrom(request);
		return ResponseEntity.ok(watchlistCompareService.compare(investorId, ciks));
	}

	private UUID investorIdFrom(HttpServletRequest request) {
		return (UUID) request.getAttribute(InvestorProvisioningFilter.INVESTOR_ID_ATTR);
	}
}
