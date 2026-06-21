package com.ssv.portfolio.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Edits a held position. The company is fixed at creation, so only the held
 * quantity and the operation date can change.
 */
public record ModifyPositionRequest(
		@NotNull(message = "quantity is required") @Positive(message = "must be positive") Integer quantity,
		@NotNull(message = "operationDate is required") LocalDate operationDate) {
}
