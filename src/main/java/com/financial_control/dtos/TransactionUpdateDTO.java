package com.financial_control.dtos;

import java.time.LocalDate;

public record TransactionUpdateDTO(
		Long id,
		Long creditCardBillId,
		String name,
		String description,
		LocalDate date,
		boolean installmentPurchase,
		Integer installmentCount,
		Double price,
		Double installmentPrice,
		Integer installmentNumber
		) {

}
