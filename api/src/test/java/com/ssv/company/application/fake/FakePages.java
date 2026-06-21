package com.ssv.company.application.fake;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.ssv.shared.dto.PageResponse;

/**
 * Test helper: slices an in-memory list into a {@link PageResponse} honoring
 * the given {@link Pageable}.
 */
final class FakePages {

	private FakePages() {
	}

	static <T> PageResponse<T> of(List<T> rows, Pageable pageable) {
		int from = (int) Math.min(pageable.getOffset(), rows.size());
		int to = Math.min(from + pageable.getPageSize(), rows.size());
		List<T> content = rows.subList(from, to);
		int totalPages = pageable.getPageSize() == 0
				? 1
				: (int) Math.ceil((double) rows.size() / pageable.getPageSize());
		return new PageResponse<>(content, pageable.getPageNumber(), pageable.getPageSize(), rows.size(), totalPages);
	}
}
