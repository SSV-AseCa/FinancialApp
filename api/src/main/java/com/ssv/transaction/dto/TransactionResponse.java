package com.ssv.transaction.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.ssv.transaction.domain.TransactionType;

public record TransactionResponse(UUID id, UUID portfolioId, String cik, int quantity, TransactionType type,
		LocalDate transactionDate) {
}
