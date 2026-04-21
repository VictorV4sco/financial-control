package com.financial_control.services.transaction_flow;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.financial_control.dtos.CreditCardBillInsertDTO;
import com.financial_control.dtos.CreditCardInsertDTO;
import com.financial_control.dtos.TransactionInsertDTO;
import com.financial_control.entities.CreditCardBill;
import com.financial_control.entities.Transaction;
import com.financial_control.repositories.CreditCardBillRepository;
import com.financial_control.repositories.CreditCardRepository;
import com.financial_control.repositories.TransactionRepository;
import com.financial_control.services.CreditCardBillService;
import com.financial_control.services.CreditCardService;
import com.financial_control.services.TransactionService;
import com.financial_control.services.exceptions.ResourceNotFoundException;

@SpringBootTest
@ActiveProfiles("test")
class CreditCardTransactionFlowIT {

	@Autowired
	private CreditCardService creditCardService;

	@Autowired
	private CreditCardBillService creditCardBillService;

	@Autowired
	private TransactionService transactionService;

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
	void shouldDeleteTransactionsWhenDeletingCreditCardBill() {
		Long creditCardId = creditCardService.insertCreditCard(new CreditCardInsertDTO(null, "Nubank")).id();

		Long billId = creditCardBillService.insertCreditCardBill(new CreditCardBillInsertDTO(
				null,
				creditCardId,
				LocalDate.of(2026, 4, 1),
				LocalDate.of(2026, 4, 25),
				LocalDate.of(2026, 5, 5),
				null,
				null)).id();

		transactionService.insertTransaction(new TransactionInsertDTO(
				null,
				billId,
				"Headphone",
				"Bluetooth headphone",
				LocalDate.of(2026, 4, 15),
				false,
				1,
				600.0));

		List<Transaction> transactionsBeforeDelete = transactionRepository.findAllByCreditCardBillId(billId);
		assertFalse(transactionsBeforeDelete.isEmpty());

		assertDoesNotThrow(() -> creditCardBillService.deleteCreditCardBill(billId));

		assertFalse(creditCardBillRepository.existsById(billId));
		assertTrueNoTransactionsForBill(billId);
	}

	@Test
	void shouldDeleteTransactionsAndBillsWhenDeletingCreditCard() {
		Long creditCardId = creditCardService.insertCreditCard(new CreditCardInsertDTO(null, "Inter")).id();

		Long firstBillId = creditCardBillService.insertCreditCardBill(new CreditCardBillInsertDTO(
				null,
				creditCardId,
				LocalDate.of(2026, 4, 1),
				LocalDate.of(2026, 4, 25),
				LocalDate.of(2026, 5, 5),
				null,
				null)).id();
		Long secondBillId = creditCardBillService.insertCreditCardBill(new CreditCardBillInsertDTO(
				null,
				creditCardId,
				LocalDate.of(2026, 5, 1),
				LocalDate.of(2026, 5, 25),
				LocalDate.of(2026, 6, 5),
				null,
				null)).id();

		transactionService.insertTransaction(new TransactionInsertDTO(
				null,
				firstBillId,
				"Notebook",
				"Work purchase",
				LocalDate.of(2026, 4, 15),
				true,
				2,
				3000.0));

		assertDoesNotThrow(() -> creditCardService.deleteCreditCard(creditCardId));

		assertFalse(creditCardRepository.existsById(creditCardId));
		assertFalse(creditCardBillRepository.existsById(firstBillId));
		assertFalse(creditCardBillRepository.existsById(secondBillId));
		assertTrueNoTransactionsForBill(firstBillId);
		assertTrueNoTransactionsForBill(secondBillId);
		assertThrows(
				ResourceNotFoundException.class,
				() -> creditCardBillService.findByCreditCardAndMonthAndYear(creditCardId, 2026, 4));
	}

	private void assertTrueNoTransactionsForBill(Long billId) {
		List<Transaction> remainingTransactions = transactionRepository.findAllByCreditCardBillId(billId);
		assertFalse(remainingTransactions.size() > 0);
	}
}
