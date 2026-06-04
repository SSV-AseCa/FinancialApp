package com.ssv.investor.fake;

import java.util.UUID;

import com.ssv.investor.application.InvestorProvisioningService;

public class FakeInvestorProvisioningService extends InvestorProvisioningService {

	private UUID responseId;
	private String lastSub;
	public boolean called;

	public FakeInvestorProvisioningService() {
		super(null, null);
	}

	public void respondWith(UUID id) {
		this.responseId = id;
	}

	@Override
	public UUID provisionIfAbsent(String sub) {
		this.lastSub = sub;
		this.called = true;
		return responseId;
	}

	public String lastSub() {
		return lastSub;
	}
}
