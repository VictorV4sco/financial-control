package com.financial_control.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreditCardInsertDTO(
		Long id,

		@NotBlank(message = "Name is required")
		@Size(min = 2, max = 20, message = "Name must be between 2 and 20 characters")
		String name
		) {

}
