package com.ssv.watchlist.fake;

import com.ssv.watchlist.application.WatchlistQueryService;
import com.ssv.watchlist.dto.WatchlistCompanyResponse;
import java.util.List;
import java.util.UUID;

public class FakeWatchlistQueryService extends WatchlistQueryService {

	private List<WatchlistCompanyResponse> listResponse;

	public FakeWatchlistQueryService() {
		super(null, null, null);
	}

	public void respondWithList(List<WatchlistCompanyResponse> list) {
		this.listResponse = list;
	}

	public void reset() {
		this.listResponse = null;
	}

	@Override
	public List<WatchlistCompanyResponse> getWatchlist(UUID investorId) {
		if (listResponse == null) {
			return List.of();
		}
		return listResponse;
	}
}