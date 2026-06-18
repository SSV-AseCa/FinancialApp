package com.ssv.transaction.fake;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.ssv.transaction.application.TransactionHistoryService;
import com.ssv.transaction.dto.TransactionResponse;

public class FakeTransactionHistoryService extends TransactionHistoryService {

	private List<TransactionResponse> history = Collections.emptyList();

	public FakeTransactionHistoryService() {
		super(null, null);
	}

	public void respondWith(List<TransactionResponse> h) {
		this.history = h;
	}

	public void reset() {
		this.history = Collections.emptyList();
	}

	@Override
	public List<TransactionResponse> getHistory(UUID investorId) {
		return history;
	}
}
