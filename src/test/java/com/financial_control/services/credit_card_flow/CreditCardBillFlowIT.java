package com.financial_control.services.credit_card_flow;

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
import com.financial_control.dtos.CreditCardInsertDTO;
import com.financial_control.dtos.CreditCardUpdateDTO;
import com.financial_control.repositories.CreditCardBillRepository;
import com.financial_control.repositories.CreditCardRepository;
import com.financial_control.repositories.TransactionRepository;
import com.financial_control.services.CreditCardBillService;
import com.financial_control.services.CreditCardService;
import com.financial_control.services.exceptions.DatabaseException;

@SpringBootTest
@ActiveProfiles("test")
class CreditCardBillFlowIT {

	@Autowired
	private CreditCardService creditCardService;

	@Autowired
	private CreditCardBillService creditCardBillService;

	@Autowired
	private CreditCardRepository creditCardRepository;

	@Autowired
	private CreditCardBillRepository creditCardBillRepository;

	@Autowired
	private TransactionRepository transactionRepository;

	@BeforeEach
	void setUp() {
		transactionRepository.deleteAll();
		creditCardBillRepository.deleteAll();
		creditCardRepository.deleteAll();
	}

	@Test
	void shouldCreateCreditCardThenCreateBillAndFindItByMonthAndYear() {
		CreditCardInsertDTO createdCard = creditCardService.insertCreditCard(new CreditCardInsertDTO(null, "Nubank"));

		CreditCardBillInsertDTO createdBill = creditCardBillService.insertCreditCardBill(
				new CreditCardBillInsertDTO(
						null,
						createdCard.id(),
						LocalDate.of(2026, 4, 1),
						LocalDate.of(2026, 4, 25),
						LocalDate.of(2026, 5, 5),
						null,
						null));

		List<CreditCardBillReadDTO> result = creditCardBillService.findByCreditCardAndMonthAndYear(
				createdCard.id(),
				2026,
				4);

		assertEquals(1, result.size());
		assertEquals(createdCard.id(), result.get(0).creditCardId());
		assertEquals(createdBill.id(), result.get(0).id());
		assertEquals(0.0, result.get(0).totalAmount());
	}

	@Test
	void shouldKeepBillsAssociatedAfterUpdatingCreditCardName() {
		CreditCardInsertDTO createdCard = creditCardService.insertCreditCard(new CreditCardInsertDTO(null, "Inter"));

		creditCardBillService.insertCreditCardBill(
				new CreditCardBillInsertDTO(
						null,
						createdCard.id(),
						LocalDate.of(2026, 6, 1),
						LocalDate.of(2026, 6, 25),
						LocalDate.of(2026, 7, 5),
						null,
						null));

		CreditCardUpdateDTO updatedCard = creditCardService.updateCreditCard(
				createdCard.id(),
				new CreditCardInsertDTO(null, "Inter Black"));

		List<CreditCardBillReadDTO> result = creditCardBillService.findByCreditCardAndMonthAndYear(
				createdCard.id(),
				2026,
				6);

		assertEquals("Inter Black", updatedCard.name());
		assertEquals(1, result.size());
		assertEquals(createdCard.id(), result.get(0).creditCardId());
	}

	@Test
	void shouldReturnOnlyBillsForRequestedCreditCard() {
		CreditCardInsertDTO firstCard = creditCardService.insertCreditCard(new CreditCardInsertDTO(null, "Card One"));
		CreditCardInsertDTO secondCard = creditCardService.insertCreditCard(new CreditCardInsertDTO(null, "Card Two"));

		creditCardBillService.insertCreditCardBill(
				new CreditCardBillInsertDTO(
						null,
						firstCard.id(),
						LocalDate.of(2026, 8, 1),
						LocalDate.of(2026, 8, 25),
						LocalDate.of(2026, 9, 5),
						null,
						null));

		creditCardBillService.insertCreditCardBill(
				new CreditCardBillInsertDTO(
						null,
						secondCard.id(),
						LocalDate.of(2026, 8, 2),
						LocalDate.of(2026, 8, 25),
						LocalDate.of(2026, 9, 5),
						null,
						null));

		List<CreditCardBillReadDTO> result = creditCardBillService.findByCreditCardAndMonthAndYear(
				firstCard.id(),
				2026,
				8);

		assertEquals(1, result.size());
		assertEquals(firstCard.id(), result.get(0).creditCardId());
	}

	@Test
	void shouldNotDeleteCreditCardWhenItHasOpenBills() {
		CreditCardInsertDTO createdCard = creditCardService.insertCreditCard(new CreditCardInsertDTO(null, "Delete Flow"));

		CreditCardBillInsertDTO createdBill = creditCardBillService.insertCreditCardBill(
				new CreditCardBillInsertDTO(
						null,
						createdCard.id(),
						LocalDate.of(2026, 10, 1),
						LocalDate.of(2026, 10, 25),
						LocalDate.of(2026, 11, 5),
						null,
						null));

		DatabaseException exception = assertThrows(
				DatabaseException.class,
				() -> creditCardService.deleteCreditCard(createdCard.id()));

		assertEquals("Credit card cannot be deleted because it has open bills", exception.getMessage());
		assertTrue(creditCardRepository.existsById(createdCard.id()));
		assertTrue(creditCardBillRepository.existsById(createdBill.id()));
		List<CreditCardBillReadDTO> result = creditCardBillService.findByCreditCardAndMonthAndYear(createdCard.id(), 2026, 10);
		assertEquals(1, result.size());
	}
}
