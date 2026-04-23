package com.financial_control.services;

import java.util.Objects;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.financial_control.dtos.CreditCardBillInsertDTO;
import com.financial_control.dtos.CreditCardBillReadDTO;
import com.financial_control.dtos.CreditCardBillUpdateDTO;
import com.financial_control.entities.CreditCard;
import com.financial_control.entities.CreditCardBill;
import com.financial_control.enums.PaymentStatus;
import com.financial_control.repositories.CreditCardBillRepository;
import com.financial_control.repositories.CreditCardRepository;
import com.financial_control.repositories.TransactionRepository;
import com.financial_control.services.exceptions.DatabaseException;
import com.financial_control.services.exceptions.ResourceNotFoundException;

@Service
public class CreditCardBillService {

	@Autowired
	private CreditCardBillRepository creditCardBillRepository;

	@Autowired
	private CreditCardRepository creditCardRepository;

	@Autowired
	private TransactionRepository transactionRepository;

	@Transactional(readOnly = true)
	public List<CreditCardBillReadDTO> findByCreditCardAndMonthAndYear(Long creditCardId, Integer year, Integer month) {
		List<CreditCardBillReadDTO> list =
				creditCardBillRepository.findByCreditCardAndMonthAndYear(creditCardId, year, month);

		if (list.isEmpty()) {
			throw new ResourceNotFoundException("No credit card bills found for this card, month and year");
		}

		return list;
	}

	@Transactional
	public CreditCardBillInsertDTO insertCreditCardBill(CreditCardBillInsertDTO dto) {
		CreditCard creditCard = creditCardRepository.findById(dto.creditCardId())
				.orElseThrow(() -> new ResourceNotFoundException("Credit card ID not found"));

		CreditCardBill bill = new CreditCardBill();
		bill.setOpeningDate(dto.openingDate());
		bill.setClosingDate(dto.closingDate());
		bill.setDueDate(dto.dueDate());
		bill.setTotalAmount(0.0);
		bill.setStatus(PaymentStatus.PENDING);
		creditCard.addBill(bill);

		CreditCardBill billSaved = creditCardBillRepository.save(bill);
		return new CreditCardBillInsertDTO(
				billSaved.getId(),
				billSaved.getCreditCard().getId(),
				billSaved.getOpeningDate(),
				billSaved.getClosingDate(),
				billSaved.getDueDate(),
				billSaved.getTotalAmount(),
				billSaved.getStatus());
	}

	@Transactional
	public CreditCardBillUpdateDTO updateCreditCardBill(Long id, CreditCardBillInsertDTO dto) {
		CreditCardBill bill = creditCardBillRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Credit card bill ID not found"));
		CreditCard creditCard = creditCardRepository.findById(dto.creditCardId())
				.orElseThrow(() -> new ResourceNotFoundException("Credit card ID not found"));

		validateDateChangeAllowed(bill, dto);

		bill.setCreditCard(creditCard);
		bill.setOpeningDate(dto.openingDate());
		bill.setClosingDate(dto.closingDate());
		bill.setDueDate(dto.dueDate());
		bill.setStatus(dto.status() == null ? bill.getStatus() : dto.status());

		CreditCardBill billSaved = creditCardBillRepository.save(bill);
		return new CreditCardBillUpdateDTO(
				billSaved.getId(),
				billSaved.getCreditCard().getId(),
				billSaved.getOpeningDate(),
				billSaved.getClosingDate(),
				billSaved.getDueDate(),
				billSaved.getTotalAmount(),
				billSaved.getStatus());
	}

	private void validateDateChangeAllowed(CreditCardBill bill, CreditCardBillInsertDTO dto) {
		boolean datesChanged =
				!Objects.equals(bill.getOpeningDate(), dto.openingDate())
				|| !Objects.equals(bill.getClosingDate(), dto.closingDate())
				|| !Objects.equals(bill.getDueDate(), dto.dueDate());

		if (datesChanged && !transactionRepository.findAllByCreditCardBillId(bill.getId()).isEmpty()) {
			throw new DatabaseException("Credit card bill dates cannot be changed because it has transactions");
		}
	}

	@Transactional
	public void deleteCreditCardBill(Long id) {
		CreditCardBill bill = creditCardBillRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Credit card bill ID not found"));

		if (!transactionRepository.findAllByCreditCardBillId(id).isEmpty()) {
			throw new DatabaseException("Credit card bill cannot be deleted because it has transactions");
		}

		creditCardBillRepository.delete(bill);
	}
}
