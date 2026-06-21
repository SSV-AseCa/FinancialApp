package com.ssv.company.infrastructure.web;

import com.ssv.company.application.CompanyFilingsService;
import com.ssv.company.application.CompanyHistoryService;
import com.ssv.company.application.CompanyMetricsService;
import com.ssv.company.application.CompanySearchService;
import com.ssv.company.dto.CompanyHistoryPoint;
import com.ssv.company.dto.CompanySearchResult;
import com.ssv.company.dto.FinancialMetricResponse;
import com.ssv.company.dto.SecFilingResponse;
import com.ssv.shared.dto.PageResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
public class CompanyController {

	private final CompanySearchService companySearchService;
	private final CompanyHistoryService companyHistoryService;
	private final CompanyMetricsService companyMetricsService;
	private final CompanyFilingsService companyFilingsService;

	@GetMapping("/search")
	public ResponseEntity<List<CompanySearchResult>> search(@RequestParam(required = false) String q) {
		if (q == null || q.isBlank()) {
			return ResponseEntity.badRequest().build();
		}
		return ResponseEntity.ok(companySearchService.searchCompanies(q.strip()));
	}

	@GetMapping("/{cik}/history")
	public ResponseEntity<List<CompanyHistoryPoint>> history(@PathVariable String cik) {
		return ResponseEntity.ok(companyHistoryService.historyByCik(cik));
	}

	@GetMapping("/{cik}/metrics")
	public ResponseEntity<PageResponse<FinancialMetricResponse>> metrics(@PathVariable String cik,
			@RequestParam(required = false) String q, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("metric").ascending());
		return ResponseEntity.ok(companyMetricsService.getMetrics(cik, q, pageable));
	}

	@GetMapping("/{cik}/filings")
	public ResponseEntity<PageResponse<SecFilingResponse>> filings(@PathVariable String cik,
			@RequestParam(required = false) String q, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("filingDate").descending());
		return ResponseEntity.ok(companyFilingsService.getFilings(cik, q, pageable));
	}
}
