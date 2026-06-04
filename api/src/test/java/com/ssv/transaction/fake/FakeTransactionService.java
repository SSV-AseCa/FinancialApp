package com.ssv.transaction.fake;

import java.util.UUID;

import com.ssv.transaction.application.TransactionService;
import com.ssv.transaction.dto.BuyRequest;
import com.ssv.transaction.dto.SellRequest;
import com.ssv.transaction.dto.TransactionResponse;

public class FakeTransactionService extends TransactionService {

	private TransactionResponse response;
	private RuntimeException error;

	public FakeTransactionService() {
		super(null, null, null);
	}

	public void respondWith(TransactionResponse r) {
		this.response = r;
	}

	public void throwOnNextCall(RuntimeException e) {
		this.error = e;
	}

	public void reset() {
		response = null;
		error = null;
	}

	@Override
	public TransactionResponse buy(UUID investorId, BuyRequest req) {
		if (error != null) {
			RuntimeException e = error;
			error = null;
			throw e;
		}
		return response;
	}

	@Override
	public TransactionResponse sell(UUID investorId, SellRequest req) {
		if (error != null) {
			RuntimeException e = error;
			error = null;
			throw e;
		}
		return response;
	}
}
