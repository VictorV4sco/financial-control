package com.financial_control.dtos;

import java.time.LocalDate;

import com.financial_control.enums.PaymentStatus;

public record CreditCardBillUpdateDTO(
		Long id,
		Long creditCardId,
		LocalDate openingDate,
		LocalDate closingDate,
		LocalDate dueDate,
		Double totalAmount,
		PaymentStatus status
		) {

}
