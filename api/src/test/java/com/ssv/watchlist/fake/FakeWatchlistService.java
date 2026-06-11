package com.ssv.watchlist.fake;

import java.util.UUID;

import com.ssv.watchlist.application.WatchlistService;
import com.ssv.watchlist.dto.AddWatchlistRequest;
import com.ssv.watchlist.dto.WatchlistResponse;

public class FakeWatchlistService extends WatchlistService {

	private WatchlistResponse response;
	private RuntimeException error;

	public FakeWatchlistService() {
		super(null, null);
	}

	public void respondWith(WatchlistResponse r) {
		this.response = r;
		this.error = null;
	}
	public void respondWithError(RuntimeException e) {
		this.error = e;
		this.response = null;
	}
	public void reset() {
		this.response = null;
		this.error = null;
	}

	@Override
	public WatchlistResponse addToWatchlist(UUID investorId, AddWatchlistRequest request) {
		if (error != null) {
			RuntimeException e = error;
			error = null;
			throw e;
		}
		return response;
	}

	@Override
	public void removeFromWatchlist(UUID investorId, String cik) {
		if (error != null) {
			RuntimeException e = error;
			error = null;
			throw e;
		}
	}
}
