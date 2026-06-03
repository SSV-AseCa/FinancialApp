package com.ssv.company.application;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssv.company.dto.CompanySearchResult;
import com.ssv.config.CacheConfig;
import com.ssv.edgar.application.EdgarClient;
import com.ssv.edgar.infrastructure.config.EdgarProperties;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "ObjectMapper is a Spring-managed singleton — storing it is safe")
public class CompanyResearchService {

	private final EdgarClient edgarClient;

	@Qualifier("searchEdgarClient")
	private final EdgarClient searchEdgarClient;

	private final EdgarProperties properties;
	private final ObjectMapper objectMapper;

	public String fetchCompanySubmissions(String cik) {
		String normalizedCik = cik.strip();
		String path = properties.submissionsPath().formatted(normalizedCik);
		return edgarClient.get(path);
	}

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
		} catch (EdgarParseException e) {
			throw e;
		} catch (Exception e) {
			throw new EdgarParseException("Failed to parse EDGAR search response", e);
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
		String name = source.path("entity_name").asText(null);
		String cik = source.path("entity_id").asText(null);
		if (name == null || cik == null) {
			return Optional.empty();
		}
		return Optional.of(new CompanySearchResult(name, cik));
	}
}
