package com.ssv.company.infrastructure.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ssv.company.application.CompanySearchService;
import com.ssv.company.dto.CompanySearchResult;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
public class CompanyController {

	private final CompanySearchService companySearchService;

	@org.springframework.beans.factory.annotation.Autowired(required = false)
	private com.ssv.company.application.CompanyHistoryService companyHistoryService;

	@GetMapping("/search")
	public ResponseEntity<List<CompanySearchResult>> search(@RequestParam(required = false) String q) {
		if (q == null || q.isBlank()) {
			return ResponseEntity.badRequest().build();
		}
		return ResponseEntity.ok(companySearchService.searchCompanies(q.strip()));
	}

	@GetMapping("/{cik}/history")
	public ResponseEntity<List<com.ssv.company.dto.CompanyHistoryPoint>> history(
			@org.springframework.web.bind.annotation.PathVariable String cik) {
		return ResponseEntity.ok(companyHistoryService.historyByCik(cik));
	}
}
