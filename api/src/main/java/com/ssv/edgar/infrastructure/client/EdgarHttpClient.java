package com.ssv.edgar.infrastructure.client;

import com.ssv.edgar.application.EdgarClient;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestClient;

@SuppressFBWarnings(
		value = "EI_EXPOSE_REP2",
		justification = "RestClient is managed by Spring and intentionally shared"
)
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