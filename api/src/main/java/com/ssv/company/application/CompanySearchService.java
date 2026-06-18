package com.ssv.company.application;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssv.company.dto.CompanySearchResult;
import com.ssv.company.exceptions.CompanySearchParseException;
import com.ssv.config.CacheConfig;
import com.ssv.edgar.application.EdgarClient;
import com.ssv.edgar.infrastructure.config.EdgarProperties;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "ObjectMapper is a Spring-managed singleton — storing it is safe")
public class CompanySearchService {

	/** Trailing "(CIK 0000320193)" segment of an EDGAR display name. */
	private static final Pattern CIK_SUFFIX = Pattern.compile("\\s*\\(CIK\\s*\\d+\\)\\s*$");

	/** Trailing "(TICKER)" segment, once the CIK suffix has been removed. */
	private static final Pattern TRAILING_PARENS = Pattern.compile("\\s*\\(([^()]*)\\)\\s*$");

	@Qualifier("searchEdgarClient")
	private final EdgarClient searchEdgarClient;

	private final EdgarProperties properties;
	private final ObjectMapper objectMapper;

	@Cacheable(CacheConfig.COMPANY_SEARCH_CACHE)
	public List<CompanySearchResult> searchCompanies(String query) {
		String path = properties.searchPath() + "?q=" + query;
		String json = searchEdgarClient.get(path);
		return parseSearchResponse(json);
	}

	private List<CompanySearchResult> parseSearchResponse(String json) {
		try {
			JsonNode hits = objectMapper.readTree(json).path("hits").path("hits");
			return extractResults(hits);
		} catch (CompanySearchParseException e) {
			throw e;
		} catch (Exception e) {
			throw new CompanySearchParseException("Failed to parse EDGAR search response", e);
		}
	}

	private List<CompanySearchResult> extractResults(JsonNode hits) {
		Map<String, CompanySearchResult> seen = new LinkedHashMap<>();
		for (JsonNode hit : hits) {
			toResult(hit).ifPresent(r -> seen.putIfAbsent(r.cik(), r));
		}
		return List.copyOf(seen.values());
	}

	private Optional<CompanySearchResult> toResult(JsonNode hit) {
		JsonNode source = hit.path("_source");
		String cik = source.path("ciks").path(0).asText(null);
		String displayName = source.path("display_names").path(0).asText(null);
		if (cik == null || displayName == null) {
			return Optional.empty();
		}
		return Optional.of(buildResult(cik, displayName));
	}

	/**
	 * EDGAR packs name, ticker and CIK into a single display name, e.g. "APPLE
	 * COMPUTER INC (AAPL) (CIK 0000320193)". The ticker segment is absent for
	 * entities without one.
	 */
	private CompanySearchResult buildResult(String cik, String displayName) {
		String withoutCik = CIK_SUFFIX.matcher(displayName).replaceFirst("");
		Matcher ticker = TRAILING_PARENS.matcher(withoutCik);
		if (ticker.find()) {
			String name = withoutCik.substring(0, ticker.start()).strip();
			return new CompanySearchResult(name, cik, tickersOf(ticker.group(1)));
		}
		return new CompanySearchResult(withoutCik.strip(), cik, List.of());
	}

	private List<String> tickersOf(String ticker) {
		String trimmed = ticker.strip();
		return trimmed.isEmpty() ? List.of() : List.of(trimmed);
	}
}
