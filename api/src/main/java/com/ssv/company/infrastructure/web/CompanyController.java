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

	@GetMapping("/search")
	public ResponseEntity<List<CompanySearchResult>> search(@RequestParam(required = false) String q) {
		if (q == null || q.isBlank()) {
			return ResponseEntity.badRequest().build();
		}
		return ResponseEntity.ok(companySearchService.searchCompanies(q.strip()));
	}
}
