package com.financialapp.edgar.infrastructure.client;

import com.financialapp.edgar.application.EdgarClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
public class EdgarHttpClient implements EdgarClient {

	private final RestClient restClient;

	@Override
	public String get(String path) {
		try {
			return restClient.get().uri(path).retrieve().body(String.class);
		} catch (Exception e) {
			return e.getMessage();
		}
	}
}
