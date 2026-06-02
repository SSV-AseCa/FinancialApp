package com.ssv.portfolio.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddPositionRequest(@NotBlank(message = "ticker must not be blank") String ticker,
		@NotNull(message = "quantity is required") @Positive(message = "must be positive") Integer quantity,
		@NotNull(message = "operationDate is required") LocalDate operationDate) {
}
