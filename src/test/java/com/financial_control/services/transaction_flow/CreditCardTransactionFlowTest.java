package com.financial_control.services.transaction_flow;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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

import com.financial_control.entities.CreditCard;
import com.financial_control.entities.CreditCardBill;
import com.financial_control.entities.Transaction;
import com.financial_control.enums.PaymentStatus;
import com.financial_control.repositories.CreditCardBillRepository;
import com.financial_control.repositories.CreditCardRepository;
import com.financial_control.repositories.TransactionRepository;
import com.financial_control.services.CreditCardService;

@ExtendWith(MockitoExtension.class)
class CreditCardTransactionFlowTest {

	@Mock
	private CreditCardRepository creditCardRepository;

	@Mock
	private CreditCardBillRepository creditCardBillRepository;

	@Mock
	private TransactionRepository transactionRepository;

	@InjectMocks
	private CreditCardService creditCardService;

	@Test
	void deleteCreditCardShouldDeleteTransactionsBeforeDeletingBillsAndCard() {
		Long creditCardId = 1L;
		CreditCard creditCard = new CreditCard(creditCardId, "Nubank");

		CreditCardBill firstBill = new CreditCardBill(
				10L,
				LocalDate.of(2026, 4, 1),
				LocalDate.of(2026, 4, 16),
				LocalDate.of(2026, 4, 25),
				1000.0,
				PaymentStatus.PENDING);
		CreditCardBill secondBill = new CreditCardBill(
				11L,
				LocalDate.of(2026, 5, 1),
				LocalDate.of(2026, 5, 16),
				LocalDate.of(2026, 5, 25),
				500.0,
				PaymentStatus.PENDING);

		creditCard.addBill(firstBill);
		creditCard.addBill(secondBill);

		Transaction firstTransaction = new Transaction(
				100L,
				"Notebook",
				"Work purchase",
				LocalDate.of(2026, 4, 15),
				true,
				2,
				3000.0,
				1500.0,
				1);
		firstTransaction.setCreditCardBill(firstBill);

		Transaction secondTransaction = new Transaction(
				101L,
				"Notebook",
				"Work purchase",
				LocalDate.of(2026, 5, 15),
				true,
				2,
				3000.0,
				1500.0,
				2);
		secondTransaction.setCreditCardBill(secondBill);

		when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));

		assertDoesNotThrow(() -> creditCardService.deleteCreditCard(creditCardId));

		verify(transactionRepository).deleteByCreditCardBillId(firstBill.getId());
		verify(creditCardBillRepository).delete(firstBill);
		verify(transactionRepository).deleteByCreditCardBillId(secondBill.getId());
		verify(creditCardBillRepository).delete(secondBill);
		verify(creditCardRepository).delete(creditCard);
	}
}
