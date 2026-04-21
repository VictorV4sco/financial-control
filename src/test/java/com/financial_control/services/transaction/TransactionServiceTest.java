package com.financial_control.services.transaction;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.financial_control.dtos.TransactionInsertDTO;
import com.financial_control.dtos.TransactionReadDTO;
import com.financial_control.dtos.TransactionUpdateDTO;
import com.financial_control.entities.CreditCardBill;
import com.financial_control.entities.Transaction;
import com.financial_control.enums.PaymentStatus;
import com.financial_control.repositories.CreditCardBillRepository;
import com.financial_control.repositories.TransactionRepository;
import com.financial_control.services.TransactionService;
import com.financial_control.services.exceptions.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

	@Mock
	private TransactionRepository transactionRepository;

	@Mock
	private CreditCardBillRepository creditCardBillRepository;

	@InjectMocks
	private TransactionService transactionService;

	@Test
	void findByCreditCardBillShouldReturnTransactionsWhenRepositoryFindsResults() {
		Long creditCardBillId = 1L;
		List<TransactionReadDTO> expectedList = List.of(
				new TransactionReadDTO(
						1L,
						creditCardBillId,
						"Notebook",
						"Work purchase",
						LocalDate.of(2026, 4, 15),
						false,
						1,
						3500.0,
						3500.0,
						1));

		when(transactionRepository.findByCreditCardBillId(creditCardBillId)).thenReturn(expectedList);

		List<TransactionReadDTO> result = transactionService.findByCreditCardBill(creditCardBillId);

		assertEquals(expectedList, result);
		verify(transactionRepository).findByCreditCardBillId(creditCardBillId);
	}

	@Test
	void findByCreditCardBillShouldThrowExceptionWhenRepositoryReturnsEmptyList() {
		Long creditCardBillId = 1L;

		when(transactionRepository.findByCreditCardBillId(creditCardBillId)).thenReturn(List.of());

		ResourceNotFoundException exception = assertThrows(
				ResourceNotFoundException.class,
				() -> transactionService.findByCreditCardBill(creditCardBillId));

		assertEquals("No transactions found for this bill", exception.getMessage());
		verify(transactionRepository).findByCreditCardBillId(creditCardBillId);
	}

	@Test
	void insertTransactionShouldSaveSingleTransactionWhenPurchaseIsNotInstallment() {
		Long creditCardBillId = 1L;
		CreditCardBill bill = new CreditCardBill(
				creditCardBillId,
				LocalDate.of(2026, 4, 1),
				LocalDate.of(2026, 4, 16),
				LocalDate.of(2026, 4, 25),
				0.0,
				PaymentStatus.PENDING);
		TransactionInsertDTO dto = new TransactionInsertDTO(
				null,
				creditCardBillId,
				"Headphone",
				"Bluetooth headphone",
				LocalDate.of(2026, 4, 15),
				false,
				1,
				600.0);
		Transaction savedTransaction = new Transaction(
				1L,
				"Headphone",
				"Bluetooth headphone",
				LocalDate.of(2026, 4, 15),
				false,
				1,
				600.0,
				600.0,
				1);
		savedTransaction.setCreditCardBill(bill);

		when(creditCardBillRepository.findById(creditCardBillId)).thenReturn(Optional.of(bill));
		when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

		TransactionInsertDTO result = transactionService.insertTransaction(dto);

		assertEquals(1L, result.id());
		assertEquals("Headphone", result.name());
		assertEquals(false, result.installmentPurchase());
		assertEquals(1, result.installmentCount());
		assertEquals(600.0, result.price());
		verify(creditCardBillRepository).findById(creditCardBillId);
		verify(transactionRepository).save(any(Transaction.class));
	}

	@Test
	void insertTransactionShouldCreateInstallmentsAcrossCurrentAndNextBills() {
		Long firstBillId = 1L;
		Long secondBillId = 2L;
		Long thirdBillId = 3L;

		CreditCardBill firstBill = new CreditCardBill(
				firstBillId,
				LocalDate.of(2026, 4, 1),
				LocalDate.of(2026, 4, 16),
				LocalDate.of(2026, 4, 25),
				0.0,
				PaymentStatus.PENDING);
		CreditCardBill secondBill = new CreditCardBill(
				secondBillId,
				LocalDate.of(2026, 5, 1),
				LocalDate.of(2026, 5, 16),
				LocalDate.of(2026, 5, 25),
				0.0,
				PaymentStatus.PENDING);
		CreditCardBill thirdBill = new CreditCardBill(
				thirdBillId,
				LocalDate.of(2026, 6, 1),
				LocalDate.of(2026, 6, 16),
				LocalDate.of(2026, 6, 25),
				0.0,
				PaymentStatus.PENDING);

		TransactionInsertDTO dto = new TransactionInsertDTO(
				null,
				firstBillId,
				"Notebook",
				"Work purchase",
				LocalDate.of(2026, 4, 15),
				true,
				3,
				3000.0);

		when(creditCardBillRepository.findById(firstBillId)).thenReturn(Optional.of(firstBill));
		when(creditCardBillRepository.findNextBills(firstBillId, 2)).thenReturn(List.of(secondBill, thirdBill));
		when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

		TransactionInsertDTO result = transactionService.insertTransaction(dto);

		assertEquals("Notebook", result.name());
		assertEquals(true, result.installmentPurchase());
		assertEquals(3, result.installmentCount());
		assertEquals(3000.0, result.price());
		verify(creditCardBillRepository).findById(firstBillId);
		verify(creditCardBillRepository).findNextBills(firstBillId, 2);
		verify(transactionRepository, times(3)).save(any(Transaction.class));
	}

	@Test
	void insertTransactionShouldThrowExceptionWhenThereAreNotEnoughFutureBillsForInstallments() {
		Long firstBillId = 1L;
		CreditCardBill firstBill = new CreditCardBill(
				firstBillId,
				LocalDate.of(2026, 4, 1),
				LocalDate.of(2026, 4, 16),
				LocalDate.of(2026, 4, 25),
				0.0,
				PaymentStatus.PENDING);
		TransactionInsertDTO dto = new TransactionInsertDTO(
				null,
				firstBillId,
				"Phone",
				"New phone",
				LocalDate.of(2026, 4, 15),
				true,
				6,
				2400.0);

		when(creditCardBillRepository.findById(firstBillId)).thenReturn(Optional.of(firstBill));
		when(creditCardBillRepository.findNextBills(firstBillId, 5)).thenReturn(List.of());

		ResourceNotFoundException exception = assertThrows(
				ResourceNotFoundException.class,
				() -> transactionService.insertTransaction(dto));

		assertEquals("Not enough future bills to generate all installments", exception.getMessage());
		verify(creditCardBillRepository).findById(firstBillId);
		verify(creditCardBillRepository).findNextBills(firstBillId, 5);
		verify(transactionRepository, never()).save(any(Transaction.class));
	}

	@Test
	void insertTransactionShouldThrowExceptionWhenCurrentBillDoesNotExist() {
		Long creditCardBillId = 1L;
		TransactionInsertDTO dto = new TransactionInsertDTO(
				null,
				creditCardBillId,
				"Headphone",
				"Bluetooth headphone",
				LocalDate.of(2026, 4, 15),
				false,
				1,
				600.0);

		when(creditCardBillRepository.findById(creditCardBillId)).thenReturn(Optional.empty());

		ResourceNotFoundException exception = assertThrows(
				ResourceNotFoundException.class,
				() -> transactionService.insertTransaction(dto));

		assertEquals("Credit card bill ID not found", exception.getMessage());
		verify(creditCardBillRepository).findById(creditCardBillId);
		verify(transactionRepository, never()).save(any(Transaction.class));
	}

	@Test
	void updateTransactionShouldUpdateAndReturnDtoWhenTransactionExists() {
		Long id = 1L;
		CreditCardBill bill = new CreditCardBill(
				1L,
				LocalDate.of(2026, 4, 1),
				LocalDate.of(2026, 4, 16),
				LocalDate.of(2026, 4, 25),
				0.0,
				PaymentStatus.PENDING);
		TransactionInsertDTO dto = new TransactionInsertDTO(
				null,
				1L,
				"Mouse",
				"Updated description",
				LocalDate.of(2026, 4, 15),
				false,
				1,
				250.0);
		Transaction existingTransaction = new Transaction(
				id,
				"Old Mouse",
				"Old description",
				LocalDate.of(2026, 4, 10),
				false,
				1,
				200.0,
				200.0,
				1);
		existingTransaction.setCreditCardBill(bill);
		Transaction updatedTransaction = new Transaction(
				id,
				"Mouse",
				"Updated description",
				LocalDate.of(2026, 4, 15),
				false,
				1,
				250.0,
				250.0,
				1);
		updatedTransaction.setCreditCardBill(bill);

		when(transactionRepository.findById(id)).thenReturn(Optional.of(existingTransaction));
		when(transactionRepository.save(existingTransaction)).thenReturn(updatedTransaction);

		TransactionUpdateDTO result = transactionService.updateTransaction(id, dto);

		assertEquals(id, result.id());
		assertEquals("Mouse", result.name());
		assertEquals(250.0, result.price());
		verify(transactionRepository).findById(id);
		verify(transactionRepository).save(existingTransaction);
	}

	@Test
	void updateTransactionShouldThrowExceptionWhenTransactionDoesNotExist() {
		Long id = 1L;
		TransactionInsertDTO dto = new TransactionInsertDTO(
				null,
				1L,
				"Mouse",
				"Updated description",
				LocalDate.of(2026, 4, 15),
				false,
				1,
				250.0);

		when(transactionRepository.findById(id)).thenReturn(Optional.empty());

		ResourceNotFoundException exception = assertThrows(
				ResourceNotFoundException.class,
				() -> transactionService.updateTransaction(id, dto));

		assertEquals("Transaction ID not found", exception.getMessage());
		verify(transactionRepository).findById(id);
		verify(transactionRepository, never()).save(any(Transaction.class));
	}

	@Test
	void deleteTransactionShouldRemoveTransactionWhenIdExists() {
		Long id = 1L;
		CreditCardBill bill = new CreditCardBill(
				1L,
				LocalDate.of(2026, 4, 1),
				LocalDate.of(2026, 4, 16),
				LocalDate.of(2026, 4, 25),
				250.0,
				PaymentStatus.PENDING);
		Transaction transaction = new Transaction(
				id,
				"Mouse",
				"Gaming mouse",
				LocalDate.of(2026, 4, 15),
				false,
				1,
				250.0,
				250.0,
				1);
		transaction.setCreditCardBill(bill);

		when(transactionRepository.findById(id)).thenReturn(Optional.of(transaction));

		assertDoesNotThrow(() -> transactionService.deleteTransaction(id));

		assertEquals(0.0, bill.getTotalAmount());
		verify(transactionRepository).findById(id);
		verify(transactionRepository).delete(transaction);
	}

	@Test
	void deleteTransactionShouldThrowExceptionWhenTransactionDoesNotExist() {
		Long id = 1L;

		when(transactionRepository.findById(id)).thenReturn(Optional.empty());

		ResourceNotFoundException exception = assertThrows(
				ResourceNotFoundException.class,
				() -> transactionService.deleteTransaction(id));

		assertEquals("Transaction ID not found", exception.getMessage());
		verify(transactionRepository).findById(id);
		verify(transactionRepository, never()).delete(any(Transaction.class));
	}
}
