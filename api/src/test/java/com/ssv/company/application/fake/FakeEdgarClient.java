package com.ssv.company.application.fake;

import com.ssv.edgar.application.EdgarClient;

public class FakeEdgarClient implements EdgarClient {

	private final String response;
	private String receivedPath;

	public FakeEdgarClient(String response) {
		this.response = response;
	}

	@Override
	public String get(String path) {
		this.receivedPath = path;
		return response;
	}

	public String receivedPath() {
		return receivedPath;
	}
}
