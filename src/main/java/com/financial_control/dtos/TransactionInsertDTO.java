package com.financial_control.dtos;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransactionInsertDTO(
		Long id,

		@NotNull(message = "Credit card bill id is required")
		Long creditCardBillId,

		@NotBlank(message = "Name is required")
		String name,

		String description,

		@NotNull(message = "Date is required")
		LocalDate date,

		boolean installmentPurchase,

		@NotNull(message = "Installment count is required")
		@Min(value = 1, message = "Installment count must be greater than zero")
		Integer installmentCount,

		@NotNull(message = "Price is required")
		@Positive(message = "Price must be greater than zero")
		Double price
		) {

}
