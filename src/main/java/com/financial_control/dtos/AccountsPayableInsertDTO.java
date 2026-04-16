package com.financial_control.dtos;

import java.time.LocalDate;

import com.financial_control.enums.PaymentStatus;

public record AccountsPayableInsertDTO(
		Long id,
		String description,
		Double amount,
		LocalDate dueDate,
		PaymentStatus status
		) {

}
