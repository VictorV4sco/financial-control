package com.financial_control.dtos;

import java.time.LocalDate;

import com.financial_control.enums.PaymentStatus;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AccountsPayableInsertDTO(
		Long id,
		
		@NotBlank(message = "Description is required")
		@Size(min = 3, max = 15, message = "Description must be between 3 and 15 characters")
		String description,
		
		@NotNull(message = "Amount is required")
		@Positive(message = "Amount must be greater than zero")
		@Digits(integer = 10, fraction = 2, message = "Invalid monetary value")
		Double amount,
		
		@NotNull(message = "Due date is required")
		@FutureOrPresent(message = "Due date cannot be in the past")
		LocalDate dueDate,
		
		PaymentStatus status
		) {

}
