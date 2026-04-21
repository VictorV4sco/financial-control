package com.financial_control.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.financial_control.dtos.CreditCardInsertDTO;
import com.financial_control.dtos.CreditCardReadDTO;
import com.financial_control.dtos.CreditCardUpdateDTO;
import com.financial_control.entities.CreditCard;
import com.financial_control.repositories.CreditCardBillRepository;
import com.financial_control.repositories.CreditCardRepository;
import com.financial_control.repositories.TransactionRepository;
import com.financial_control.services.exceptions.DatabaseException;
import com.financial_control.services.exceptions.ResourceNotFoundException;

@Service
public class CreditCardService {

	@Autowired
	private CreditCardRepository creditCardRepository;

	@Autowired
	private CreditCardBillRepository creditCardBillRepository;

	@Autowired
	private TransactionRepository transactionRepository;

	@Transactional(readOnly = true)
	public List<CreditCardReadDTO> findAllCreditCard() {
		return creditCardRepository.findAllCreditCard();
	}

	@Transactional
	public CreditCardInsertDTO insertCreditCard(CreditCardInsertDTO dto) {
		CreditCard creditCard = new CreditCard();
		creditCard.setName(dto.name());

		CreditCard creditCardSaved = creditCardRepository.save(creditCard);
		return new CreditCardInsertDTO(creditCardSaved.getId(), creditCardSaved.getName());
	}

	@Transactional
	public CreditCardUpdateDTO updateCreditCard(Long id, CreditCardInsertDTO dto) {
		CreditCard creditCard = creditCardRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("ID not found"));
		creditCard.setName(dto.name());

		CreditCard creditCardSaved = creditCardRepository.save(creditCard);
		return new CreditCardUpdateDTO(creditCardSaved.getId(), creditCardSaved.getName());
	}

	@Transactional
	public void deleteCreditCard(Long id) {
		CreditCard creditCard = creditCardRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Recurso não encontrado"));
		try {
			List.copyOf(new ArrayList<>(creditCard.getBill())).forEach(bill -> {
				transactionRepository.deleteByCreditCardBillId(bill.getId());
				creditCard.removeBill(bill);
				creditCardBillRepository.delete(bill);
			});
			creditCardRepository.delete(creditCard);
		}
		catch (DataIntegrityViolationException e) {
			throw new DatabaseException("Falha de integridade referencial");
		}
	}
}
