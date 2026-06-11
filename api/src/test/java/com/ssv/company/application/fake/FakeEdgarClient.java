package com.ssv.company.application.fake;

import com.ssv.edgar.application.EdgarClient;

public class FakeEdgarClient implements EdgarClient {

	private String response;
	private String receivedPath;

	public FakeEdgarClient() {}
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

	public void setResponse(String response) {
		this.response = response;
	}
}
