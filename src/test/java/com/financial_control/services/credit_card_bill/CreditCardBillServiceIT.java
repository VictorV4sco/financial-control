package com.financial_control.services.credit_card_bill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.financial_control.dtos.CreditCardBillInsertDTO;
import com.financial_control.dtos.CreditCardBillReadDTO;
import com.financial_control.entities.CreditCard;
import com.financial_control.entities.CreditCardBill;
import com.financial_control.enums.PaymentStatus;
import com.financial_control.repositories.CreditCardBillRepository;
import com.financial_control.repositories.CreditCardRepository;
import com.financial_control.services.CreditCardBillService;
import com.financial_control.services.exceptions.ResourceNotFoundException;

import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
class CreditCardBillServiceIT {

	@Autowired
	private CreditCardBillService creditCardBillService;

	@Autowired
	private CreditCardBillRepository creditCardBillRepository;

	@Autowired
	private CreditCardRepository creditCardRepository;

	@BeforeEach
	void setUp() {
		creditCardBillRepository.deleteAll();
		creditCardRepository.deleteAll();
	}

	@Test
	void insertCreditCardBillShouldPersistBillWithPendingStatusAndZeroTotalAmount() {
		CreditCard creditCard = creditCardRepository.save(new CreditCard(null, "Nubank"));
		CreditCardBillInsertDTO dto = new CreditCardBillInsertDTO(
				null,
				creditCard.getId(),
				LocalDate.of(2026, 4, 1),
				LocalDate.now().plusDays(5),
				LocalDate.now().plusDays(10),
				null,
				null);

		CreditCardBillInsertDTO result = creditCardBillService.insertCreditCardBill(dto);

		CreditCardBill savedBill = creditCardBillRepository.findById(result.id()).orElseThrow();

		assertEquals(creditCard.getId(), result.creditCardId());
		assertEquals(0.0, result.totalAmount());
		assertEquals(PaymentStatus.PENDING, result.status());
		assertEquals(dto.openingDate(), savedBill.getOpeningDate());
		assertEquals(dto.closingDate(), savedBill.getClosingDate());
		assertEquals(dto.dueDate(), savedBill.getDueDate());
		assertEquals(PaymentStatus.PENDING, savedBill.getStatus());
	}

	@Test
	void insertCreditCardBillShouldThrowExceptionWhenCreditCardDoesNotExist() {
		CreditCardBillInsertDTO dto = new CreditCardBillInsertDTO(
				null,
				999L,
				LocalDate.of(2026, 4, 1),
				LocalDate.now().plusDays(5),
				LocalDate.now().plusDays(10),
				null,
				null);

		ResourceNotFoundException exception = assertThrows(
				ResourceNotFoundException.class,
				() -> creditCardBillService.insertCreditCardBill(dto));

		assertEquals("Credit card ID not found", exception.getMessage());
	}

	@Test
	void findByCreditCardAndMonthAndYearShouldReturnPersistedBills() {
		CreditCard creditCard = creditCardRepository.save(new CreditCard(null, "Inter"));

		CreditCardBill firstBill = new CreditCardBill(
				null,
				LocalDate.of(2026, 4, 1),
				LocalDate.of(2026, 4, 25),
				LocalDate.of(2026, 5, 5),
				0.0,
				PaymentStatus.PENDING);
		firstBill.setCreditCard(creditCard);

		CreditCardBill secondBill = new CreditCardBill(
				null,
				LocalDate.of(2026, 4, 2),
				LocalDate.of(2026, 4, 25),
				LocalDate.of(2026, 5, 5),
				120.0,
				PaymentStatus.PAID);
		secondBill.setCreditCard(creditCard);

		creditCardBillRepository.save(firstBill);
		creditCardBillRepository.save(secondBill);

		List<CreditCardBillReadDTO> result = creditCardBillService.findByCreditCardAndMonthAndYear(
				creditCard.getId(),
				2026,
				4);

		assertEquals(2, result.size());
		assertEquals(creditCard.getId(), result.get(0).creditCardId());
	}

	@Test
	void findByCreditCardAndMonthAndYearShouldThrowExceptionWhenNoBillsExist() {
		CreditCard creditCard = creditCardRepository.save(new CreditCard(null, "Inter"));

		ResourceNotFoundException exception = assertThrows(
				ResourceNotFoundException.class,
				() -> creditCardBillService.findByCreditCardAndMonthAndYear(creditCard.getId(), 2026, 4));

		assertEquals("No credit card bills found for this card, month and year", exception.getMessage());
	}
}
