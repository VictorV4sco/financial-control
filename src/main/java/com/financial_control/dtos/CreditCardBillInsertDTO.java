package com.financial_control.dtos;

import java.time.LocalDate;

import com.financial_control.enums.PaymentStatus;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreditCardBillInsertDTO(
		Long id,

		@NotNull(message = "Credit card id is required")
		@Positive
		Long creditCardId,

		@NotNull(message = "Opening date is required")
		LocalDate openingDate,

		@NotNull(message = "Closing date is required")
		@FutureOrPresent(message = "Closing date must be in the future")
		LocalDate closingDate,

		@NotNull(message = "Due date is required")
		@FutureOrPresent(message = "Due date must be in the future")
		LocalDate dueDate,

		Double totalAmount,
		PaymentStatus status
		) {

}
