package com.ssv.shared.dto;

import java.util.List;

import org.springframework.data.domain.Page;

/**
 * Transport envelope for a single page of results. Decouples the API contract
 * from Spring Data's {@link Page} (whose JSON serialization is unstable across
 * versions) while carrying the metadata clients need to render pagination.
 */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

	public PageResponse {
		content = List.copyOf(content);
	}

	public static <T> PageResponse<T> of(Page<T> page) {
		return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(),
				page.getTotalPages());
	}
}
