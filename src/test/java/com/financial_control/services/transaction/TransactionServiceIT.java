package com.financial_control.services.transaction;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.financial_control.dtos.TransactionInsertDTO;
import com.financial_control.dtos.TransactionReadDTO;
import com.financial_control.dtos.TransactionUpdateDTO;
import com.financial_control.entities.CreditCard;
import com.financial_control.entities.CreditCardBill;
import com.financial_control.entities.Transaction;
import com.financial_control.enums.PaymentStatus;
import com.financial_control.repositories.CreditCardBillRepository;
import com.financial_control.repositories.CreditCardRepository;
import com.financial_control.repositories.TransactionRepository;
import com.financial_control.services.TransactionService;
import com.financial_control.services.exceptions.ResourceNotFoundException;

import jakarta.transaction.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TransactionServiceIT {

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private TransactionRepository transactionRepository;

	@Autowired
	private CreditCardBillRepository creditCardBillRepository;

	@Autowired
	private CreditCardRepository creditCardRepository;

	@BeforeEach
	void setUp() {
		transactionRepository.deleteAll();
		creditCardBillRepository.deleteAll();
		creditCardRepository.deleteAll();
	}

	@Test
	void insertTransactionShouldPersistSingleTransactionAndUpdateBillTotalAmount() {
		CreditCardBill currentBill = createBillChain("Nubank", List.of(
				LocalDate.of(2026, 4, 1),
				LocalDate.of(2026, 4, 16),
				LocalDate.of(2026, 4, 25)))
				.getFirst();

		TransactionInsertDTO dto = new TransactionInsertDTO(
				null,
				currentBill.getId(),
				"Headphone",
				"Bluetooth headphone",
				LocalDate.of(2026, 4, 15),
				false,
				1,
				600.0);

		TransactionInsertDTO result = transactionService.insertTransaction(dto);
		List<Transaction> savedTransactions = transactionRepository.findAll();
		CreditCardBill updatedBill = creditCardBillRepository.findById(currentBill.getId()).orElseThrow();

		assertEquals("Headphone", result.name());
		assertEquals(1, savedTransactions.size());
		assertEquals(600.0, updatedBill.getTotalAmount());
		assertEquals(600.0, savedTransactions.getFirst().getInstallmentPrice());
		assertEquals(1, savedTransactions.getFirst().getInstallmentNumber());
	}

	@Test
	void insertTransactionShouldPersistInstallmentsAcrossCurrentAndNextBills() {
		List<CreditCardBill> bills = createBillChain("Inter", List.of(
				LocalDate.of(2026, 4, 1),
				LocalDate.of(2026, 4, 16),
				LocalDate.of(2026, 4, 25),
				LocalDate.of(2026, 5, 1),
				LocalDate.of(2026, 5, 16),
				LocalDate.of(2026, 5, 25),
				LocalDate.of(2026, 6, 1),
				LocalDate.of(2026, 6, 16),
				LocalDate.of(2026, 6, 25)));

		CreditCardBill firstBill = bills.get(0);
		CreditCardBill secondBill = bills.get(1);
		CreditCardBill thirdBill = bills.get(2);

		TransactionInsertDTO dto = new TransactionInsertDTO(
				null,
				firstBill.getId(),
				"Notebook",
				"Work purchase",
				LocalDate.of(2026, 4, 15),
				true,
				3,
				3000.0);

		TransactionInsertDTO result = transactionService.insertTransaction(dto);
		List<Transaction> savedTransactions = transactionRepository.findAll();
		CreditCardBill updatedFirstBill = creditCardBillRepository.findById(firstBill.getId()).orElseThrow();
		CreditCardBill updatedSecondBill = creditCardBillRepository.findById(secondBill.getId()).orElseThrow();
		CreditCardBill updatedThirdBill = creditCardBillRepository.findById(thirdBill.getId()).orElseThrow();

		assertEquals(dto, result);
		assertEquals(3, savedTransactions.size());
		assertEquals(1000.0, updatedFirstBill.getTotalAmount());
		assertEquals(1000.0, updatedSecondBill.getTotalAmount());
		assertEquals(1000.0, updatedThirdBill.getTotalAmount());
	}

	@Test
	void findByCreditCardBillShouldReturnPersistedTransactions() {
		CreditCardBill currentBill = createBillChain("XP", List.of(
				LocalDate.of(2026, 4, 1),
				LocalDate.of(2026, 4, 16),
				LocalDate.of(2026, 4, 25)))
				.getFirst();

		transactionRepository.save(createTransaction(
				currentBill,
				"Mouse",
				"Gaming mouse",
				LocalDate.of(2026, 4, 10),
				false,
				1,
				250.0,
				250.0,
				1));
		transactionRepository.save(createTransaction(
				currentBill,
				"Keyboard",
				"Mechanical keyboard",
				LocalDate.of(2026, 4, 12),
				true,
				3,
				900.0,
				300.0,
				2));

		List<TransactionReadDTO> result = transactionService.findByCreditCardBill(currentBill.getId());

		assertEquals(2, result.size());
		assertEquals(currentBill.getId(), result.get(0).creditCardBillId());
	}

	@Test
	void findByCreditCardBillShouldThrowExceptionWhenNoTransactionsExist() {
		CreditCardBill currentBill = createBillChain("BTG", List.of(
				LocalDate.of(2026, 4, 1),
				LocalDate.of(2026, 4, 16),
				LocalDate.of(2026, 4, 25)))
				.getFirst();

		ResourceNotFoundException exception = assertThrows(
				ResourceNotFoundException.class,
				() -> transactionService.findByCreditCardBill(currentBill.getId()));

		assertEquals("No transactions found for this bill", exception.getMessage());
	}

	@Test
	void updateTransactionShouldPersistUpdatedDataAndAdjustBillTotalAmount() {
		CreditCardBill currentBill = createBillChain("Will", List.of(
				LocalDate.of(2026, 4, 1),
				LocalDate.of(2026, 4, 16),
				LocalDate.of(2026, 4, 25)))
				.getFirst();
		currentBill.setTotalAmount(200.0);

		Transaction transaction = transactionRepository.save(createTransaction(
				currentBill,
				"Mouse",
				"Old description",
				LocalDate.of(2026, 4, 10),
				false,
				1,
				200.0,
				200.0,
				1));

		TransactionInsertDTO dto = new TransactionInsertDTO(
				null,
				currentBill.getId(),
				"Mouse Updated",
				"Updated description",
				LocalDate.of(2026, 4, 15),
				false,
				1,
				250.0);

		TransactionUpdateDTO result = transactionService.updateTransaction(transaction.getId(), dto);
		Transaction updatedTransaction = transactionRepository.findById(transaction.getId()).orElseThrow();
		CreditCardBill updatedBill = creditCardBillRepository.findById(currentBill.getId()).orElseThrow();

		assertEquals("Mouse Updated", result.name());
		assertEquals(250.0, result.price());
		assertEquals("Mouse Updated", updatedTransaction.getName());
		assertEquals(250.0, updatedBill.getTotalAmount());
	}

	@Test
	void deleteTransactionShouldRemoveTransactionAndSubtractValueFromBillTotalAmount() {
		CreditCardBill currentBill = createBillChain("C6", List.of(
				LocalDate.of(2026, 4, 1),
				LocalDate.of(2026, 4, 16),
				LocalDate.of(2026, 4, 25)))
				.getFirst();
		currentBill.setTotalAmount(250.0);

		Transaction transaction = transactionRepository.save(createTransaction(
				currentBill,
				"Mouse",
				"Gaming mouse",
				LocalDate.of(2026, 4, 15),
				false,
				1,
				250.0,
				250.0,
				1));

		assertDoesNotThrow(() -> transactionService.deleteTransaction(transaction.getId()));

		CreditCardBill updatedBill = creditCardBillRepository.findById(currentBill.getId()).orElseThrow();
		assertFalse(transactionRepository.existsById(transaction.getId()));
		assertEquals(0.0, updatedBill.getTotalAmount());
	}

	private List<CreditCardBill> createBillChain(String cardName, List<LocalDate> dates) {
		CreditCard creditCard = creditCardRepository.save(new CreditCard(null, cardName));

		CreditCardBill firstBill = new CreditCardBill(
				null,
				dates.get(0),
				dates.get(1),
				dates.get(2),
				0.0,
				PaymentStatus.PENDING);
		firstBill.setCreditCard(creditCard);

		List<CreditCardBill> bills = List.of(firstBill);

		if (dates.size() >= 6) {
			CreditCardBill secondBill = new CreditCardBill(
					null,
					dates.get(3),
					dates.get(4),
					dates.get(5),
					0.0,
					PaymentStatus.PENDING);
			secondBill.setCreditCard(creditCard);

			if (dates.size() >= 9) {
				CreditCardBill thirdBill = new CreditCardBill(
						null,
						dates.get(6),
						dates.get(7),
						dates.get(8),
						0.0,
						PaymentStatus.PENDING);
				thirdBill.setCreditCard(creditCard);
				bills = List.of(firstBill, secondBill, thirdBill);
				return creditCardBillRepository.saveAll(bills);
			}

			bills = List.of(firstBill, secondBill);
		}

		return creditCardBillRepository.saveAll(bills);
	}

	private Transaction createTransaction(CreditCardBill bill, String name, String description, LocalDate date,
			boolean installmentPurchase, Integer installmentCount, Double price, Double installmentPrice,
			Integer installmentNumber) {
		Transaction transaction = new Transaction(
				null,
				name,
				description,
				date,
				installmentPurchase,
				installmentCount,
				price,
				installmentPrice,
				installmentNumber);
		transaction.setCreditCardBill(bill);
		return transaction;
	}
}
