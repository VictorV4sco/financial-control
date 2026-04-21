package com.financial_control.services.credit_card;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.financial_control.dtos.CreditCardInsertDTO;
import com.financial_control.dtos.CreditCardReadDTO;
import com.financial_control.dtos.CreditCardUpdateDTO;
import com.financial_control.entities.CreditCard;
import com.financial_control.repositories.CreditCardRepository;
import com.financial_control.services.CreditCardService;
import com.financial_control.services.exceptions.DatabaseException;
import com.financial_control.services.exceptions.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class CreditCardServiceTest {

	@Mock
	private CreditCardRepository creditCardRepository;

	@InjectMocks
	private CreditCardService creditCardService;

	@Test
	void findAllCreditCardShouldReturnListWhenRepositoryFindsResults() {
		List<CreditCardReadDTO> expectedList = List.of(
				new CreditCardReadDTO(1L, "Nubank"),
				new CreditCardReadDTO(2L, "Inter"));

		when(creditCardRepository.findAllCreditCard()).thenReturn(expectedList);

		List<CreditCardReadDTO> result = creditCardService.findAllCreditCard();

		assertEquals(expectedList, result);
		verify(creditCardRepository).findAllCreditCard();
	}

	@Test
	void insertCreditCardShouldSaveCardAndReturnDto() {
		CreditCardInsertDTO dto = new CreditCardInsertDTO(null, "Nubank");
		CreditCard savedCard = new CreditCard(1L, "Nubank");

		when(creditCardRepository.save(any(CreditCard.class))).thenReturn(savedCard);

		CreditCardInsertDTO result = creditCardService.insertCreditCard(dto);

		assertEquals(1L, result.id());
		assertEquals("Nubank", result.name());
		verify(creditCardRepository).save(any(CreditCard.class));
	}

	@Test
	void updateCreditCardShouldUpdateAndReturnDtoWhenCardExists() {
		Long id = 1L;
		CreditCardInsertDTO dto = new CreditCardInsertDTO(null, "Inter Black");
		CreditCard existingCard = new CreditCard(id, "Inter");
		CreditCard updatedCard = new CreditCard(id, "Inter Black");

		when(creditCardRepository.findById(id)).thenReturn(Optional.of(existingCard));
		when(creditCardRepository.save(existingCard)).thenReturn(updatedCard);

		CreditCardUpdateDTO result = creditCardService.updateCreditCard(id, dto);

		assertEquals(id, result.id());
		assertEquals("Inter Black", result.name());
		verify(creditCardRepository).findById(id);
		verify(creditCardRepository).save(existingCard);
	}

	@Test
	void updateCreditCardShouldThrowExceptionWhenCardDoesNotExist() {
		Long id = 1L;
		CreditCardInsertDTO dto = new CreditCardInsertDTO(null, "Inter Black");

		when(creditCardRepository.findById(id)).thenReturn(Optional.empty());

		ResourceNotFoundException exception = assertThrows(
				ResourceNotFoundException.class,
				() -> creditCardService.updateCreditCard(id, dto));

		assertEquals("ID not found", exception.getMessage());
		verify(creditCardRepository).findById(id);
		verify(creditCardRepository, never()).save(any(CreditCard.class));
	}

	@Test
	void deleteCreditCardShouldRemoveCardWhenIdExists() {
		Long id = 1L;

		when(creditCardRepository.existsById(id)).thenReturn(true);

		assertDoesNotThrow(() -> creditCardService.deleteCreditCard(id));

		verify(creditCardRepository).existsById(id);
		verify(creditCardRepository).deleteById(id);
	}

	@Test
	void deleteCreditCardShouldThrowExceptionWhenIdDoesNotExist() {
		Long id = 1L;

		when(creditCardRepository.existsById(id)).thenReturn(false);

		assertThrows(ResourceNotFoundException.class, () -> creditCardService.deleteCreditCard(id));

		verify(creditCardRepository).existsById(id);
		verify(creditCardRepository, never()).deleteById(id);
	}

	@Test
	void deleteCreditCardShouldThrowDatabaseExceptionWhenDeleteViolatesIntegrity() {
		Long id = 1L;

		when(creditCardRepository.existsById(id)).thenReturn(true);
		org.mockito.Mockito.doThrow(new DataIntegrityViolationException("constraint"))
				.when(creditCardRepository).deleteById(id);

		assertThrows(DatabaseException.class, () -> creditCardService.deleteCreditCard(id));

		verify(creditCardRepository).existsById(id);
		verify(creditCardRepository).deleteById(id);
	}
}
