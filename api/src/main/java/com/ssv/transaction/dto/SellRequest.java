package com.ssv.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record SellRequest(@NotBlank String cik, @Positive Integer quantity) {
}
