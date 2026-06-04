package com.ssv.company.infrastructure.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ssv.company.application.CompanyRequest;
import com.ssv.company.application.CompanyResearchService;
import com.ssv.company.application.CompanySearchService;
import com.ssv.company.application.FinancialStatementStore;
import com.ssv.company.application.SecFilingStore;
import com.ssv.company.domain.Company;
import com.ssv.company.domain.FinancialStatement;
import com.ssv.company.domain.SecFiling;
import com.ssv.company.dto.CompanyDetailsResponse;
import com.ssv.company.dto.CompanySearchResult;
import com.ssv.company.dto.FinancialStatementResponse;
import com.ssv.company.dto.SecFilingResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
public class CompanyController {

	private final CompanySearchService companySearchService;
	private final CompanyResearchService companyResearchService;
	private final FinancialStatementStore financialStatementStore;
	private final SecFilingStore secFilingStore;

	@GetMapping("/search")
	public ResponseEntity<List<CompanySearchResult>> search(@RequestParam(required = false) String q) {
		if (q == null || q.isBlank()) {
			return ResponseEntity.badRequest().build();
		}
		return ResponseEntity.ok(companySearchService.searchCompanies(q.strip()));
	}

	@GetMapping("/{cik}")
	public ResponseEntity<CompanyDetailsResponse> getCompanyDetails(
			@PathVariable String cik,
			@RequestParam String name,
			@RequestParam(required = false, defaultValue = "") String symbol) {
		CompanyRequest request = new CompanyRequest(cik, symbol, name);
		Company company = companyResearchService.getOrFetchFinancialData(request).company();
		List<FinancialStatement> statements = financialStatementStore.findByCompanyId(company.getId());
		List<SecFiling> filings = secFilingStore.findByCompanyId(company.getId());
		return ResponseEntity.ok(toResponse(company, statements, filings));
	}

	private CompanyDetailsResponse toResponse(Company company, List<FinancialStatement> statements,
			List<SecFiling> filings) {
		List<FinancialStatementResponse> metrics = statements.stream()
				.map(s -> new FinancialStatementResponse(s.getMetric(), s.getValue(), s.getUnit(), s.getPeriodEnd()))
				.toList();
		List<SecFilingResponse> filingResponses = filings.stream()
				.map(f -> new SecFilingResponse(f.getFormType(), f.getFilingDate(), f.getUrl()))
				.toList();
		return new CompanyDetailsResponse(company.getCik(), company.getSymbol(), company.getName(), metrics,
				filingResponses);
	}
}
