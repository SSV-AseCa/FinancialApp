package com.ssv.watchlist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AddWatchlistRequest(
		@NotBlank @Pattern(regexp = "\\d+", message = "must contain only digits") String cik) {
}
