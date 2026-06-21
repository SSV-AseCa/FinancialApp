package com.ssv.portfolio.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * A manually recorded holding. The company is identified by CIK (as in the buy
 * flow) so it can be validated against EDGAR and resolved to its ticker symbol.
 */
public record AddPositionRequest(@NotBlank(message = "cik must not be blank") String cik,
		@NotNull(message = "quantity is required") @Positive(message = "must be positive") Integer quantity,
		@NotNull(message = "operationDate is required") LocalDate operationDate) {
}
