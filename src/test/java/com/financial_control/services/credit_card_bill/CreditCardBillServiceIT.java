package com.financial_control.services.credit_card_bill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.financial_control.dtos.CreditCardBillInsertDTO;
import com.financial_control.dtos.CreditCardBillReadDTO;
import com.financial_control.dtos.CreditCardBillUpdateDTO;
import com.financial_control.entities.CreditCard;
import com.financial_control.entities.CreditCardBill;
import com.financial_control.entities.Transaction;
import com.financial_control.enums.PaymentStatus;
import com.financial_control.repositories.CreditCardBillRepository;
import com.financial_control.repositories.CreditCardRepository;
import com.financial_control.repositories.TransactionRepository;
import com.financial_control.services.CreditCardBillService;
import com.financial_control.services.exceptions.DatabaseException;
import com.financial_control.services.exceptions.ResourceNotFoundException;

import jakarta.transaction.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CreditCardBillServiceIT {

	@Autowired
	private CreditCardBillService creditCardBillService;

	@Autowired
	private CreditCardBillRepository creditCardBillRepository;

	@Autowired
	private CreditCardRepository creditCardRepository;

	@Autowired
	private TransactionRepository transactionRepository;

	@BeforeEach
	void setUp() {
		transactionRepository.deleteAll();
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

	@Test
	void updateCreditCardBillShouldPersistUpdatedDataWithoutChangingTotalAmount() {
		CreditCard creditCard = creditCardRepository.save(new CreditCard(null, "Nubank"));
		CreditCardBill savedBill = new CreditCardBill(
				null,
				LocalDate.of(2026, 4, 1),
				LocalDate.of(2026, 4, 25),
				LocalDate.of(2026, 5, 5),
				100.0,
				PaymentStatus.PENDING);
		savedBill.setCreditCard(creditCard);
		savedBill = creditCardBillRepository.save(savedBill);

		CreditCardBillInsertDTO dto = new CreditCardBillInsertDTO(
				null,
				creditCard.getId(),
				LocalDate.of(2026, 4, 25),
				LocalDate.of(2026, 5, 25),
				LocalDate.of(2026, 6, 5),
				250.0,
				PaymentStatus.PAID);

		CreditCardBillUpdateDTO result = creditCardBillService.updateCreditCardBill(savedBill.getId(), dto);
		CreditCardBill updatedBill = creditCardBillRepository.findById(savedBill.getId()).orElseThrow();

		assertEquals(savedBill.getId(), result.id());
		assertEquals(LocalDate.of(2026, 4, 25), result.openingDate());
		assertEquals(LocalDate.of(2026, 5, 25), updatedBill.getClosingDate());
		assertEquals(LocalDate.of(2026, 6, 5), updatedBill.getDueDate());
		assertEquals(100.0, updatedBill.getTotalAmount());
		assertEquals(PaymentStatus.PAID, updatedBill.getStatus());
	}

	@Test
	void updateCreditCardBillShouldThrowExceptionWhenBillDoesNotExist() {
		CreditCard creditCard = creditCardRepository.save(new CreditCard(null, "Nubank"));
		CreditCardBillInsertDTO dto = new CreditCardBillInsertDTO(
				null,
				creditCard.getId(),
				LocalDate.of(2026, 4, 25),
				LocalDate.of(2026, 5, 25),
				LocalDate.of(2026, 6, 5),
				250.0,
				PaymentStatus.PAID);

		ResourceNotFoundException exception = assertThrows(
				ResourceNotFoundException.class,
				() -> creditCardBillService.updateCreditCardBill(999L, dto));

		assertEquals("Credit card bill ID not found", exception.getMessage());
	}

	@Test
	void updateCreditCardBillShouldThrowExceptionWhenChangingDatesAndBillHasTransactions() {
		CreditCard creditCard = creditCardRepository.save(new CreditCard(null, "Nubank"));
		CreditCardBill savedBill = new CreditCardBill(
				null,
				LocalDate.of(2026, 4, 1),
				LocalDate.of(2026, 4, 25),
				LocalDate.of(2026, 5, 5),
				1000.0,
				PaymentStatus.PENDING);
		savedBill.setCreditCard(creditCard);
		savedBill = creditCardBillRepository.save(savedBill);
		Long savedBillId = savedBill.getId();

		Transaction transaction = new Transaction(
				null,
				"Notebook",
				"Work purchase",
				LocalDate.of(2026, 4, 15),
				true,
				3,
				3000.0,
				1000.0,
				1);
		transaction.setCreditCardBill(savedBill);
		transactionRepository.save(transaction);

		CreditCardBillInsertDTO dto = new CreditCardBillInsertDTO(
				null,
				creditCard.getId(),
				LocalDate.of(2026, 4, 25),
				LocalDate.of(2026, 5, 25),
				LocalDate.of(2026, 6, 5),
				null,
				PaymentStatus.PAID);

		DatabaseException exception = assertThrows(
				DatabaseException.class,
				() -> creditCardBillService.updateCreditCardBill(savedBillId, dto));
		CreditCardBill unchangedBill = creditCardBillRepository.findById(savedBillId).orElseThrow();

		assertEquals("Credit card bill dates cannot be changed because it has transactions", exception.getMessage());
		assertEquals(LocalDate.of(2026, 4, 1), unchangedBill.getOpeningDate());
		assertEquals(LocalDate.of(2026, 4, 25), unchangedBill.getClosingDate());
		assertEquals(LocalDate.of(2026, 5, 5), unchangedBill.getDueDate());
		assertEquals(1000.0, unchangedBill.getTotalAmount());
	}

	@Test
	void updateCreditCardBillShouldAllowStatusChangeWhenBillHasTransactionsAndDatesDoNotChange() {
		CreditCard creditCard = creditCardRepository.save(new CreditCard(null, "Nubank"));
		CreditCardBill savedBill = new CreditCardBill(
				null,
				LocalDate.of(2026, 4, 1),
				LocalDate.of(2026, 4, 25),
				LocalDate.of(2026, 5, 5),
				1000.0,
				PaymentStatus.PENDING);
		savedBill.setCreditCard(creditCard);
		savedBill = creditCardBillRepository.save(savedBill);

		Transaction transaction = new Transaction(
				null,
				"Notebook",
				"Work purchase",
				LocalDate.of(2026, 4, 15),
				true,
				3,
				3000.0,
				1000.0,
				1);
		transaction.setCreditCardBill(savedBill);
		transactionRepository.save(transaction);

		CreditCardBillInsertDTO dto = new CreditCardBillInsertDTO(
				null,
				creditCard.getId(),
				LocalDate.of(2026, 4, 1),
				LocalDate.of(2026, 4, 25),
				LocalDate.of(2026, 5, 5),
				null,
				PaymentStatus.PAID);

		CreditCardBillUpdateDTO result = creditCardBillService.updateCreditCardBill(savedBill.getId(), dto);
		CreditCardBill updatedBill = creditCardBillRepository.findById(savedBill.getId()).orElseThrow();

		assertEquals(PaymentStatus.PAID, result.status());
		assertEquals(PaymentStatus.PAID, updatedBill.getStatus());
		assertEquals(1000.0, updatedBill.getTotalAmount());
	}

	@Test
	void deleteCreditCardBillShouldRemoveBillFromDatabase() {
		CreditCard creditCard = creditCardRepository.save(new CreditCard(null, "Nubank"));
		CreditCardBill savedBill = new CreditCardBill(
				null,
				LocalDate.of(2026, 4, 1),
				LocalDate.of(2026, 4, 25),
				LocalDate.of(2026, 5, 5),
				0.0,
				PaymentStatus.PENDING);
		savedBill.setCreditCard(creditCard);
		savedBill = creditCardBillRepository.save(savedBill);

		creditCardBillService.deleteCreditCardBill(savedBill.getId());

		assertThrows(ResourceNotFoundException.class,
				() -> creditCardBillService.findByCreditCardAndMonthAndYear(creditCard.getId(), 2026, 4));
	}

	@Test
	void deleteCreditCardBillShouldThrowExceptionWhenBillHasTransactions() {
		CreditCard creditCard = creditCardRepository.save(new CreditCard(null, "Nubank"));
		CreditCardBill savedBill = new CreditCardBill(
				null,
				LocalDate.of(2026, 4, 1),
				LocalDate.of(2026, 4, 25),
				LocalDate.of(2026, 5, 5),
				1000.0,
				PaymentStatus.PENDING);
		savedBill.setCreditCard(creditCard);
		savedBill = creditCardBillRepository.save(savedBill);
		Long savedBillId = savedBill.getId();

		Transaction transaction = new Transaction(
				null,
				"Notebook",
				"Work purchase",
				LocalDate.of(2026, 4, 15),
				true,
				3,
				3000.0,
				1000.0,
				1);
		transaction.setCreditCardBill(savedBill);
		transactionRepository.save(transaction);

		DatabaseException exception = assertThrows(
				DatabaseException.class,
				() -> creditCardBillService.deleteCreditCardBill(savedBillId));

		assertEquals("Credit card bill cannot be deleted because it has transactions", exception.getMessage());
		assertTrue(creditCardBillRepository.existsById(savedBillId));
		assertEquals(1, transactionRepository.findAllByCreditCardBillId(savedBillId).size());
	}
}
