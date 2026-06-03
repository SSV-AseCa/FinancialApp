package com.ssv.portfolio.dto;

import java.time.LocalDate;
import java.util.UUID;

public record PositionResponse(UUID id, String ticker, int quantity, LocalDate operationDate) {
}
