package com.financial_control.dtos;

import java.time.LocalDate;

import com.financial_control.enums.PaymentStatus;

public record AccountsPayableInsertStatusDTO(
		LocalDate dueDate,
		PaymentStatus status
		) {

}
