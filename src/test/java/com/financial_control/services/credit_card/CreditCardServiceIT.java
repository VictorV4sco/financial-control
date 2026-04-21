package com.financial_control.services.credit_card;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.financial_control.dtos.CreditCardInsertDTO;
import com.financial_control.dtos.CreditCardReadDTO;
import com.financial_control.dtos.CreditCardUpdateDTO;
import com.financial_control.entities.CreditCard;
import com.financial_control.repositories.CreditCardRepository;
import com.financial_control.services.CreditCardService;
import com.financial_control.services.exceptions.ResourceNotFoundException;

import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
class CreditCardServiceIT {

	@Autowired
	private CreditCardService creditCardService;

	@Autowired
	private CreditCardRepository creditCardRepository;

	@BeforeEach
	void setUp() {
		creditCardRepository.deleteAll();
	}

	@Test
	void findAllCreditCardShouldReturnPersistedCards() {
		creditCardRepository.save(new CreditCard(null, "Nubank"));
		creditCardRepository.save(new CreditCard(null, "Inter"));

		List<CreditCardReadDTO> result = creditCardService.findAllCreditCard();

		assertEquals(2, result.size());
		assertEquals("Nubank", result.get(0).name());
		assertEquals("Inter", result.get(1).name());
	}

	@Test
	void insertCreditCardShouldPersistCard() {
		CreditCardInsertDTO dto = new CreditCardInsertDTO(null, "C6");

		CreditCardInsertDTO result = creditCardService.insertCreditCard(dto);

		CreditCard savedCard = creditCardRepository.findById(result.id()).orElseThrow();

		assertEquals("C6", result.name());
		assertEquals(result.id(), savedCard.getId());
		assertEquals("C6", savedCard.getName());
	}

	@Test
	void updateCreditCardShouldPersistUpdatedName() {
		CreditCard savedCard = creditCardRepository.save(new CreditCard(null, "Original"));
		CreditCardInsertDTO dto = new CreditCardInsertDTO(null, "Updated");

		CreditCardUpdateDTO result = creditCardService.updateCreditCard(savedCard.getId(), dto);
		CreditCard updatedCard = creditCardRepository.findById(savedCard.getId()).orElseThrow();

		assertEquals(savedCard.getId(), result.id());
		assertEquals("Updated", result.name());
		assertEquals("Updated", updatedCard.getName());
	}

	@Test
	void updateCreditCardShouldThrowExceptionWhenCardDoesNotExist() {
		CreditCardInsertDTO dto = new CreditCardInsertDTO(null, "Updated");

		ResourceNotFoundException exception = assertThrows(
				ResourceNotFoundException.class,
				() -> creditCardService.updateCreditCard(999L, dto));

		assertEquals("ID not found", exception.getMessage());
	}

	@Test
	void deleteCreditCardShouldRemoveCardFromDatabase() {
		CreditCard savedCard = creditCardRepository.save(new CreditCard(null, "Delete Me"));

		assertDoesNotThrow(() -> creditCardService.deleteCreditCard(savedCard.getId()));

		assertFalse(creditCardRepository.existsById(savedCard.getId()));
	}
}
