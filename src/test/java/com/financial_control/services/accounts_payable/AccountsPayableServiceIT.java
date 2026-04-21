package com.financial_control.services.accounts_payable;

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

import com.financial_control.dtos.AccountsPayableInsertDTO;
import com.financial_control.dtos.AccountsPayableReadDTO;
import com.financial_control.dtos.AccountsPayableUpdateDTO;
import com.financial_control.entities.AccountsPayable;
import com.financial_control.enums.PaymentStatus;
import com.financial_control.repositories.AccountsPayableRepository;
import com.financial_control.services.AccountsPayableService;
import com.financial_control.services.exceptions.ResourceNotFoundException;

import jakarta.transaction.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AccountsPayableServiceIT {

	@Autowired
	private AccountsPayableService accountsPayableService;

	@Autowired
	private AccountsPayableRepository accountsPayableRepository;

	@BeforeEach
	void setUp() {
		accountsPayableRepository.deleteAll();
	}

	@Test
	void insertAccountPayableShouldPersistBillWithPendingStatus() {
		AccountsPayableInsertDTO dto = new AccountsPayableInsertDTO(
				null,
				"Internet",
				120.0,
				LocalDate.now().plusDays(5),
				null);

		AccountsPayableInsertDTO result = accountsPayableService.insertAccountPayable(dto);

		AccountsPayable savedBill = accountsPayableRepository.findById(result.id()).orElseThrow();

		assertEquals("Internet", result.description());
		assertEquals(120.0, result.amount());
		assertEquals(PaymentStatus.PENDING, result.status());
		assertEquals(result.id(), savedBill.getId());
		assertEquals(PaymentStatus.PENDING, savedBill.getStatus());
	}

	@Test
	void getByMonthAndYearShouldReturnBillsAndUpdateOverdueStatus() {
		LocalDate today = LocalDate.now();
		LocalDate overdueDate = today.minusDays(1);
		LocalDate futureDate = today.plusDays(1);

		accountsPayableRepository.save(new AccountsPayable(
				null,
				"Water",
				80.0,
				overdueDate,
				PaymentStatus.PENDING));
		accountsPayableRepository.save(new AccountsPayable(
				null,
				"Phone",
				95.0,
				futureDate,
				PaymentStatus.PENDING));

		List<AccountsPayableReadDTO> result = accountsPayableService.getByMonthAndYear(
				today.getMonthValue(),
				today.getYear());

		assertEquals(2, result.size());
		assertEquals(1, result.stream().filter(item -> item.status() == PaymentStatus.OVERDUE).count());
		assertEquals(1, result.stream().filter(item -> item.status() == PaymentStatus.PENDING).count());
	}

	@Test
	void getByMonthAndYearShouldThrowExceptionWhenNoBillsExistForMonthAndYear() {
		Integer month = 1;
		Integer year = 2030;

		ResourceNotFoundException exception = assertThrows(
				ResourceNotFoundException.class,
				() -> accountsPayableService.getByMonthAndYear(month, year));

		assertEquals("No accounts payable found for month 1 and year 2030", exception.getMessage());
	}

	@Test
	void updateAccountPayableShouldPersistUpdatedData() {
		AccountsPayable savedBill = accountsPayableRepository.save(new AccountsPayable(
				null,
				"Gym",
				90.0,
				LocalDate.now().plusDays(7),
				PaymentStatus.PENDING));

		AccountsPayableInsertDTO dto = new AccountsPayableInsertDTO(
				null,
				"Gym Premium",
				115.0,
				LocalDate.now().plusDays(12),
				PaymentStatus.PAID);

		AccountsPayableUpdateDTO result = accountsPayableService.updateAccountPayable(savedBill.getId(), dto);
		AccountsPayable updatedBill = accountsPayableRepository.findById(savedBill.getId()).orElseThrow();

		assertEquals("Gym Premium", result.description());
		assertEquals(115.0, result.amount());
		assertEquals(PaymentStatus.PAID, result.status());
		assertEquals("Gym Premium", updatedBill.getDescription());
		assertEquals(PaymentStatus.PAID, updatedBill.getStatus());
	}

	@Test
	void deleteShouldRemoveBillFromDatabase() {
		AccountsPayable savedBill = accountsPayableRepository.save(new AccountsPayable(
				null,
				"Streaming",
				39.9,
				LocalDate.now().plusDays(4),
				PaymentStatus.PENDING));

		assertDoesNotThrow(() -> accountsPayableService.delete(savedBill.getId()));

		assertFalse(accountsPayableRepository.existsById(savedBill.getId()));
	}
}
