package com.financial_control.services.credit_card_bill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

import com.financial_control.dtos.CreditCardBillInsertDTO;
import com.financial_control.dtos.CreditCardBillReadDTO;
import com.financial_control.dtos.CreditCardBillUpdateDTO;
import com.financial_control.entities.CreditCard;
import com.financial_control.entities.CreditCardBill;
import com.financial_control.enums.PaymentStatus;
import com.financial_control.repositories.CreditCardBillRepository;
import com.financial_control.repositories.CreditCardRepository;
import com.financial_control.repositories.TransactionRepository;
import com.financial_control.services.CreditCardBillService;
import com.financial_control.services.exceptions.DatabaseException;
import com.financial_control.services.exceptions.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class CreditCardBillServiceTest {

	@Mock
	private CreditCardBillRepository creditCardBillRepository;

	@Mock
	private CreditCardRepository creditCardRepository;

	@Mock
	private TransactionRepository transactionRepository;

	@InjectMocks
	private CreditCardBillService creditCardBillService;

	@Test
	void findByCreditCardAndMonthAndYearShouldReturnListWhenRepositoryFindsResults() {
		Long creditCardId = 1L;
		Integer year = 2026;
		Integer month = 4;
		List<CreditCardBillReadDTO> expectedList = List.of(
				new CreditCardBillReadDTO(
						1L,
						creditCardId,
						LocalDate.of(2026, 4, 1),
						LocalDate.of(2026, 4, 25),
						LocalDate.of(2026, 5, 5),
						0.0,
						PaymentStatus.PENDING));

		when(creditCardBillRepository.findByCreditCardAndMonthAndYear(creditCardId, year, month)).thenReturn(expectedList);

		List<CreditCardBillReadDTO> result =
				creditCardBillService.findByCreditCardAndMonthAndYear(creditCardId, year, month);

		assertEquals(expectedList, result);
		verify(creditCardBillRepository).findByCreditCardAndMonthAndYear(creditCardId, year, month);
	}

	@Test
	void findByCreditCardAndMonthAndYearShouldThrowExceptionWhenRepositoryReturnsEmptyList() {
		Long creditCardId = 1L;
		Integer year = 2026;
		Integer month = 4;

		when(creditCardBillRepository.findByCreditCardAndMonthAndYear(creditCardId, year, month)).thenReturn(List.of());

		ResourceNotFoundException exception = assertThrows(
				ResourceNotFoundException.class,
				() -> creditCardBillService.findByCreditCardAndMonthAndYear(creditCardId, year, month));

		assertEquals("No credit card bills found for this card, month and year", exception.getMessage());
		verify(creditCardBillRepository).findByCreditCardAndMonthAndYear(creditCardId, year, month);
	}

	@Test
	void insertCreditCardBillShouldSaveBillWithPendingStatusAndZeroTotalAmount() {
		CreditCardBillInsertDTO dto = new CreditCardBillInsertDTO(
				null,
				1L,
				LocalDate.of(2026, 4, 1),
				LocalDate.of(2026, 4, 25),
				LocalDate.of(2026, 5, 5),
				null,
				null);
		CreditCard creditCard = new CreditCard(1L, "Nubank");
		CreditCardBill savedBill = new CreditCardBill(
				10L,
				LocalDate.of(2026, 4, 1),
				LocalDate.of(2026, 4, 25),
				LocalDate.of(2026, 5, 5),
				0.0,
				PaymentStatus.PENDING);
		savedBill.setCreditCard(creditCard);

		when(creditCardRepository.findById(1L)).thenReturn(Optional.of(creditCard));
		when(creditCardBillRepository.save(any(CreditCardBill.class))).thenReturn(savedBill);

		CreditCardBillInsertDTO result = creditCardBillService.insertCreditCardBill(dto);

		assertEquals(10L, result.id());
		assertEquals(1L, result.creditCardId());
		assertEquals(LocalDate.of(2026, 4, 1), result.openingDate());
		assertEquals(LocalDate.of(2026, 4, 25), result.closingDate());
		assertEquals(LocalDate.of(2026, 5, 5), result.dueDate());
		assertEquals(0.0, result.totalAmount());
		assertEquals(PaymentStatus.PENDING, result.status());
		verify(creditCardRepository).findById(1L);
		verify(creditCardBillRepository).save(any(CreditCardBill.class));
	}

	@Test
	void insertCreditCardBillShouldThrowExceptionWhenCreditCardDoesNotExist() {
		CreditCardBillInsertDTO dto = new CreditCardBillInsertDTO(
				null,
				1L,
				LocalDate.of(2026, 4, 1),
				LocalDate.of(2026, 4, 25),
				LocalDate.of(2026, 5, 5),
				null,
				null);

		when(creditCardRepository.findById(1L)).thenReturn(Optional.empty());

		ResourceNotFoundException exception = assertThrows(
				ResourceNotFoundException.class,
				() -> creditCardBillService.insertCreditCardBill(dto));

		assertEquals("Credit card ID not found", exception.getMessage());
		verify(creditCardRepository).findById(1L);
	}

	@Test
	void updateCreditCardBillShouldUpdateEditableFieldsAndKeepTotalAmountWhenBillAndCardExist() {
		Long billId = 10L;
		CreditCard creditCard = new CreditCard(1L, "Nubank");
		CreditCardBill bill = new CreditCardBill(
				billId,
				LocalDate.of(2026, 4, 1),
				LocalDate.of(2026, 4, 25),
				LocalDate.of(2026, 5, 5),
				300.0,
				PaymentStatus.PENDING);
		bill.setCreditCard(creditCard);
		CreditCardBillInsertDTO dto = new CreditCardBillInsertDTO(
				null,
				1L,
				LocalDate.of(2026, 4, 25),
				LocalDate.of(2026, 5, 25),
				LocalDate.of(2026, 6, 5),
				500.0,
				PaymentStatus.PAID);

		when(creditCardBillRepository.findById(billId)).thenReturn(Optional.of(bill));
		when(creditCardRepository.findById(1L)).thenReturn(Optional.of(creditCard));
		when(creditCardBillRepository.save(bill)).thenReturn(bill);

		CreditCardBillUpdateDTO result = creditCardBillService.updateCreditCardBill(billId, dto);

		assertEquals(billId, result.id());
		assertEquals(1L, result.creditCardId());
		assertEquals(LocalDate.of(2026, 4, 25), result.openingDate());
		assertEquals(LocalDate.of(2026, 5, 25), result.closingDate());
		assertEquals(LocalDate.of(2026, 6, 5), result.dueDate());
		assertEquals(300.0, result.totalAmount());
		assertEquals(PaymentStatus.PAID, result.status());
		verify(creditCardBillRepository).findById(billId);
		verify(creditCardRepository).findById(1L);
		verify(creditCardBillRepository).save(bill);
	}

	@Test
	void updateCreditCardBillShouldThrowExceptionWhenBillDoesNotExist() {
		Long billId = 10L;
		CreditCardBillInsertDTO dto = new CreditCardBillInsertDTO(
				null,
				1L,
				LocalDate.of(2026, 4, 25),
				LocalDate.of(2026, 5, 25),
				LocalDate.of(2026, 6, 5),
				500.0,
				PaymentStatus.PAID);

		when(creditCardBillRepository.findById(billId)).thenReturn(Optional.empty());

		ResourceNotFoundException exception = assertThrows(
				ResourceNotFoundException.class,
				() -> creditCardBillService.updateCreditCardBill(billId, dto));

		assertEquals("Credit card bill ID not found", exception.getMessage());
		verify(creditCardBillRepository).findById(billId);
		verify(creditCardRepository, never()).findById(any());
		verify(creditCardBillRepository, never()).save(any(CreditCardBill.class));
	}

	@Test
	void updateCreditCardBillShouldThrowExceptionWhenCreditCardDoesNotExist() {
		Long billId = 10L;
		CreditCardBill bill = new CreditCardBill(
				billId,
				LocalDate.of(2026, 4, 1),
				LocalDate.of(2026, 4, 25),
				LocalDate.of(2026, 5, 5),
				300.0,
				PaymentStatus.PENDING);
		CreditCardBillInsertDTO dto = new CreditCardBillInsertDTO(
				null,
				1L,
				LocalDate.of(2026, 4, 25),
				LocalDate.of(2026, 5, 25),
				LocalDate.of(2026, 6, 5),
				500.0,
				PaymentStatus.PAID);

		when(creditCardBillRepository.findById(billId)).thenReturn(Optional.of(bill));
		when(creditCardRepository.findById(1L)).thenReturn(Optional.empty());

		ResourceNotFoundException exception = assertThrows(
				ResourceNotFoundException.class,
				() -> creditCardBillService.updateCreditCardBill(billId, dto));

		assertEquals("Credit card ID not found", exception.getMessage());
		verify(creditCardBillRepository).findById(billId);
		verify(creditCardRepository).findById(1L);
		verify(creditCardBillRepository, never()).save(any(CreditCardBill.class));
	}

	@Test
	void updateCreditCardBillShouldThrowExceptionWhenChangingDatesAndBillHasTransactions() {
		Long billId = 10L;
		CreditCard creditCard = new CreditCard(1L, "Nubank");
		CreditCardBill bill = new CreditCardBill(
				billId,
				LocalDate.of(2026, 4, 1),
				LocalDate.of(2026, 4, 25),
				LocalDate.of(2026, 5, 5),
				300.0,
				PaymentStatus.PENDING);
		bill.setCreditCard(creditCard);
		CreditCardBillInsertDTO dto = new CreditCardBillInsertDTO(
				null,
				1L,
				LocalDate.of(2026, 4, 25),
				LocalDate.of(2026, 5, 25),
				LocalDate.of(2026, 6, 5),
				null,
				PaymentStatus.PAID);

		when(creditCardBillRepository.findById(billId)).thenReturn(Optional.of(bill));
		when(creditCardRepository.findById(1L)).thenReturn(Optional.of(creditCard));
		when(transactionRepository.findAllByCreditCardBillId(billId)).thenReturn(List.of(new com.financial_control.entities.Transaction()));

		DatabaseException exception = assertThrows(
				DatabaseException.class,
				() -> creditCardBillService.updateCreditCardBill(billId, dto));

		assertEquals("Credit card bill dates cannot be changed because it has transactions", exception.getMessage());
		verify(creditCardBillRepository).findById(billId);
		verify(creditCardRepository).findById(1L);
		verify(transactionRepository).findAllByCreditCardBillId(billId);
		verify(creditCardBillRepository, never()).save(any(CreditCardBill.class));
	}

	@Test
	void updateCreditCardBillShouldAllowStatusChangeWhenBillHasTransactionsAndDatesDoNotChange() {
		Long billId = 10L;
		CreditCard creditCard = new CreditCard(1L, "Nubank");
		CreditCardBill bill = new CreditCardBill(
				billId,
				LocalDate.of(2026, 4, 1),
				LocalDate.of(2026, 4, 25),
				LocalDate.of(2026, 5, 5),
				300.0,
				PaymentStatus.PENDING);
		bill.setCreditCard(creditCard);
		CreditCardBillInsertDTO dto = new CreditCardBillInsertDTO(
				null,
				1L,
				LocalDate.of(2026, 4, 1),
				LocalDate.of(2026, 4, 25),
				LocalDate.of(2026, 5, 5),
				null,
				PaymentStatus.PAID);

		when(creditCardBillRepository.findById(billId)).thenReturn(Optional.of(bill));
		when(creditCardRepository.findById(1L)).thenReturn(Optional.of(creditCard));
		when(creditCardBillRepository.save(bill)).thenReturn(bill);

		CreditCardBillUpdateDTO result = creditCardBillService.updateCreditCardBill(billId, dto);

		assertEquals(PaymentStatus.PAID, result.status());
		assertEquals(300.0, result.totalAmount());
		verify(transactionRepository, never()).findAllByCreditCardBillId(billId);
		verify(creditCardBillRepository).save(bill);
	}

	@Test
	void deleteCreditCardBillShouldDeleteBillWhenItHasNoTransactions() {
		Long billId = 10L;
		CreditCard creditCard = new CreditCard(1L, "Nubank");
		CreditCardBill bill = new CreditCardBill(
				billId,
				LocalDate.of(2026, 4, 1),
				LocalDate.of(2026, 4, 25),
				LocalDate.of(2026, 5, 5),
				1000.0,
				PaymentStatus.PENDING);
		bill.setCreditCard(creditCard);

		when(creditCardBillRepository.findById(billId)).thenReturn(Optional.of(bill));
		when(transactionRepository.findAllByCreditCardBillId(billId)).thenReturn(List.of());

		creditCardBillService.deleteCreditCardBill(billId);

		verify(creditCardBillRepository).findById(billId);
		verify(transactionRepository).findAllByCreditCardBillId(billId);
		verify(transactionRepository, never()).deleteByCreditCardBillId(billId);
		verify(creditCardBillRepository).delete(bill);
	}

	@Test
	void deleteCreditCardBillShouldThrowDatabaseExceptionWhenBillHasTransactions() {
		Long billId = 10L;
		CreditCard creditCard = new CreditCard(1L, "Nubank");
		CreditCardBill bill = new CreditCardBill(
				billId,
				LocalDate.of(2026, 4, 1),
				LocalDate.of(2026, 4, 25),
				LocalDate.of(2026, 5, 5),
				1000.0,
				PaymentStatus.PENDING);
		bill.setCreditCard(creditCard);

		when(creditCardBillRepository.findById(billId)).thenReturn(Optional.of(bill));
		when(transactionRepository.findAllByCreditCardBillId(billId))
				.thenReturn(List.of(new com.financial_control.entities.Transaction()));

		DatabaseException exception = assertThrows(
				DatabaseException.class,
				() -> creditCardBillService.deleteCreditCardBill(billId));

		assertEquals("Credit card bill cannot be deleted because it has transactions", exception.getMessage());
		verify(creditCardBillRepository).findById(billId);
		verify(transactionRepository).findAllByCreditCardBillId(billId);
		verify(transactionRepository, never()).deleteByCreditCardBillId(any());
		verify(creditCardBillRepository, never()).delete(any(CreditCardBill.class));
	}

	@Test
	void deleteCreditCardBillShouldThrowExceptionWhenBillDoesNotExist() {
		Long billId = 10L;

		when(creditCardBillRepository.findById(billId)).thenReturn(Optional.empty());

		ResourceNotFoundException exception = assertThrows(
				ResourceNotFoundException.class,
				() -> creditCardBillService.deleteCreditCardBill(billId));

		assertEquals("Credit card bill ID not found", exception.getMessage());
		verify(creditCardBillRepository).findById(billId);
		verify(transactionRepository, never()).deleteByCreditCardBillId(any());
		verify(creditCardBillRepository, never()).delete(any(CreditCardBill.class));
	}
}
