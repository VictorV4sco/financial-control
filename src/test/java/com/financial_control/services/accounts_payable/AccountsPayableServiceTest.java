package com.financial_control.services.accounts_payable;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import org.springframework.dao.DataIntegrityViolationException;

import com.financial_control.dtos.AccountsPayableInsertDTO;
import com.financial_control.dtos.AccountsPayableReadDTO;
import com.financial_control.dtos.AccountsPayableUpdateDTO;
import com.financial_control.entities.AccountsPayable;
import com.financial_control.enums.PaymentStatus;
import com.financial_control.repositories.AccountsPayableRepository;
import com.financial_control.services.AccountsPayableService;
import com.financial_control.services.exceptions.DatabaseException;
import com.financial_control.services.exceptions.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class AccountsPayableServiceTest {

	@Mock
	private AccountsPayableRepository accountsPayableRepository;

	@InjectMocks
	private AccountsPayableService accountsPayableService;

	@Test
	void getByMonthAndYearShouldReturnBillsWhenRepositoryFindsResults() {
		Integer month = 4;
		Integer year = 2026;
		List<AccountsPayableReadDTO> expectedList = List.of(
				new AccountsPayableReadDTO(1L, "Internet", 150.0, LocalDate.of(2026, 4, 10), PaymentStatus.PENDING));

		when(accountsPayableRepository.findByMonthAndYear(month, year)).thenReturn(expectedList);

		List<AccountsPayableReadDTO> result = accountsPayableService.getByMonthAndYear(month, year);

		assertEquals(expectedList, result);
		verify(accountsPayableRepository).updateOverdueByMonthAndYear(
				eq(month), eq(year), any(LocalDate.class), eq(PaymentStatus.OVERDUE));
		verify(accountsPayableRepository).findByMonthAndYear(month, year);
	}

	@Test
	void getByMonthAndYearShouldThrowExceptionWhenRepositoryReturnsEmptyList() {
		Integer month = 4;
		Integer year = 2026;

		when(accountsPayableRepository.findByMonthAndYear(month, year)).thenReturn(List.of());

		ResourceNotFoundException exception = assertThrows(
				ResourceNotFoundException.class,
				() -> accountsPayableService.getByMonthAndYear(month, year));

		assertEquals("No accounts payable found for month 4 and year 2026", exception.getMessage());
		verify(accountsPayableRepository).updateOverdueByMonthAndYear(
				eq(month), eq(year), any(LocalDate.class), eq(PaymentStatus.OVERDUE));
		verify(accountsPayableRepository).findByMonthAndYear(month, year);
	}

	@Test
	void insertAccountPayableShouldSaveBillWithPendingStatus() {
		AccountsPayableInsertDTO dto = new AccountsPayableInsertDTO(
				null, "Electricity", 220.0, LocalDate.of(2026, 5, 8), null);
		AccountsPayable savedBill = new AccountsPayable(
				1L, "Electricity", 220.0, LocalDate.of(2026, 5, 8), PaymentStatus.PENDING);

		when(accountsPayableRepository.save(any(AccountsPayable.class))).thenReturn(savedBill);

		AccountsPayableInsertDTO result = accountsPayableService.insertAccountPayable(dto);

		assertEquals(1L, result.id());
		assertEquals("Electricity", result.description());
		assertEquals(220.0, result.amount());
		assertEquals(LocalDate.of(2026, 5, 8), result.dueDate());
		assertEquals(PaymentStatus.PENDING, result.status());
		verify(accountsPayableRepository).save(any(AccountsPayable.class));
	}

	@Test
	void updateAccountPayableShouldUpdateAndReturnDtoWhenBillExists() {
		Long id = 1L;
		AccountsPayableInsertDTO dto = new AccountsPayableInsertDTO(
				null, "Rent", 1800.0, LocalDate.of(2026, 5, 15), PaymentStatus.PAID);
		AccountsPayable existingBill = new AccountsPayable(
				id, "Old Rent", 1700.0, LocalDate.of(2026, 5, 10), PaymentStatus.PENDING);
		AccountsPayable updatedBill = new AccountsPayable(
				id, "Rent", 1800.0, LocalDate.of(2026, 5, 15), PaymentStatus.PAID);

		when(accountsPayableRepository.findById(id)).thenReturn(Optional.of(existingBill));
		when(accountsPayableRepository.save(existingBill)).thenReturn(updatedBill);

		AccountsPayableUpdateDTO result = accountsPayableService.updateAccountPayable(id, dto);

		assertEquals(id, result.id());
		assertEquals("Rent", result.description());
		assertEquals(1800.0, result.amount());
		assertEquals(LocalDate.of(2026, 5, 15), result.dueDate());
		assertEquals(PaymentStatus.PAID, result.status());
		verify(accountsPayableRepository).findById(id);
		verify(accountsPayableRepository).save(existingBill);
	}

	@Test
	void updateAccountPayableShouldThrowExceptionWhenBillDoesNotExist() {
		Long id = 1L;
		AccountsPayableInsertDTO dto = new AccountsPayableInsertDTO(
				null, "Rent", 1800.0, LocalDate.of(2026, 5, 15), PaymentStatus.PAID);

		when(accountsPayableRepository.findById(id)).thenReturn(Optional.empty());

		ResourceNotFoundException exception = assertThrows(
				ResourceNotFoundException.class,
				() -> accountsPayableService.updateAccountPayable(id, dto));

		assertEquals("ID not found", exception.getMessage());
		verify(accountsPayableRepository).findById(id);
		verify(accountsPayableRepository, never()).save(any(AccountsPayable.class));
	}

	@Test
	void deleteShouldRemoveBillWhenIdExists() {
		Long id = 1L;

		when(accountsPayableRepository.existsById(id)).thenReturn(true);

		assertDoesNotThrow(() -> accountsPayableService.delete(id));

		verify(accountsPayableRepository).existsById(id);
		verify(accountsPayableRepository).deleteById(id);
	}

	@Test
	void deleteShouldThrowExceptionWhenIdDoesNotExist() {
		Long id = 1L;

		when(accountsPayableRepository.existsById(id)).thenReturn(false);

		assertThrows(ResourceNotFoundException.class, () -> accountsPayableService.delete(id));

		verify(accountsPayableRepository).existsById(id);
		verify(accountsPayableRepository, never()).deleteById(id);
	}

	@Test
	void deleteShouldThrowDatabaseExceptionWhenDeleteViolatesIntegrity() {
		Long id = 1L;

		when(accountsPayableRepository.existsById(id)).thenReturn(true);
		org.mockito.Mockito.doThrow(new DataIntegrityViolationException("constraint"))
				.when(accountsPayableRepository).deleteById(id);

		assertThrows(DatabaseException.class, () -> accountsPayableService.delete(id));

		verify(accountsPayableRepository).existsById(id);
		verify(accountsPayableRepository).deleteById(id);
	}
}
