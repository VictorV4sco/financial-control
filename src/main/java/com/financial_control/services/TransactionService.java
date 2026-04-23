package com.financial_control.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.financial_control.dtos.TransactionInsertDTO;
import com.financial_control.dtos.TransactionReadDTO;
import com.financial_control.dtos.TransactionUpdateDTO;
import com.financial_control.entities.CreditCardBill;
import com.financial_control.entities.Transaction;
import com.financial_control.enums.PaymentStatus;
import com.financial_control.repositories.CreditCardBillRepository;
import com.financial_control.repositories.TransactionRepository;
import com.financial_control.services.exceptions.ResourceNotFoundException;

@Service
public class TransactionService {

	@Autowired
	private TransactionRepository transactionRepository;

	@Autowired
	private CreditCardBillRepository creditCardBillRepository;

	@Transactional(readOnly = true)
	public List<TransactionReadDTO> findByCreditCardBill(Long creditCardBillId) {
		List<TransactionReadDTO> list = transactionRepository.findByCreditCardBillId(creditCardBillId);

		if (list.isEmpty()) {
			throw new ResourceNotFoundException("No transactions found for this bill");
		}

		return list;
	}

	@Transactional
	public TransactionInsertDTO insertTransaction(TransactionInsertDTO dto) {
		CreditCardBill currentBill = creditCardBillRepository.findById(dto.creditCardBillId())
				.orElseThrow(() -> new ResourceNotFoundException("Credit card bill ID not found"));

		if (!dto.installmentPurchase()) {
			Transaction savedTransaction = transactionRepository.save(
					createTransaction(dto, currentBill, dto.price(), 1));
			updateBillTotalAmount(currentBill, dto.price());
			return new TransactionInsertDTO(
					savedTransaction.getId(),
					savedTransaction.getCreditCardBill().getId(),
					savedTransaction.getName(),
					savedTransaction.getDescription(),
					savedTransaction.getDate(),
					savedTransaction.isInstallmentPurchase(),
					savedTransaction.getInstallmentCount(),
					savedTransaction.getPrice());
		}

		int nextBillsQuantity = dto.installmentCount() - 1;
		List<CreditCardBill> nextBills = creditCardBillRepository.findNextBills(dto.creditCardBillId(), nextBillsQuantity);
		List<CreditCardBill> futureBills = new ArrayList<>(nextBills);
		createMissingFutureBills(currentBill, futureBills, nextBillsQuantity);

		List<CreditCardBill> targetBills = new ArrayList<>();
		targetBills.add(currentBill);
		targetBills.addAll(futureBills);

		double installmentPrice = dto.price() / dto.installmentCount();
		for (int i = 0; i < targetBills.size(); i++) {
			CreditCardBill bill = targetBills.get(i);
			transactionRepository.save(createTransaction(dto, bill, installmentPrice, i + 1));
			updateBillTotalAmount(bill, installmentPrice);
		}

		return dto;
	}

	private void createMissingFutureBills(CreditCardBill currentBill, List<CreditCardBill> futureBills,
			int expectedQuantity) {
		CreditCardBill previousBill = futureBills.isEmpty() ? currentBill : futureBills.get(futureBills.size() - 1);

		while (futureBills.size() < expectedQuantity) {
			CreditCardBill nextBill = new CreditCardBill();
			nextBill.setCreditCard(currentBill.getCreditCard());
			nextBill.setOpeningDate(previousBill.getClosingDate());
			nextBill.setClosingDate(previousBill.getClosingDate().plusMonths(1));
			nextBill.setDueDate(previousBill.getDueDate().plusMonths(1));
			nextBill.setTotalAmount(0.0);
			nextBill.setStatus(PaymentStatus.PENDING);

			CreditCardBill savedBill = creditCardBillRepository.save(nextBill);
			futureBills.add(savedBill);
			previousBill = savedBill;
		}
	}

	@Transactional
	public TransactionUpdateDTO updateTransaction(Long id, TransactionInsertDTO dto) {
		Transaction transaction = transactionRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Transaction ID not found"));
		CreditCardBill bill = transaction.getCreditCardBill();
		double previousInstallmentPrice = transaction.getInstallmentPrice() == null ? 0.0 : transaction.getInstallmentPrice();

		transaction.setName(dto.name());
		transaction.setDescription(dto.description());
		transaction.setDate(dto.date());
		transaction.setInstallmentPurchase(dto.installmentPurchase());
		transaction.setInstallmentCount(dto.installmentCount());
		transaction.setPrice(dto.price());
		transaction.setInstallmentPrice(dto.price());
		transaction.setInstallmentNumber(1);
		updateBillTotalAmount(bill, dto.price() - previousInstallmentPrice);

		Transaction savedTransaction = transactionRepository.save(transaction);
		return new TransactionUpdateDTO(
				savedTransaction.getId(),
				savedTransaction.getCreditCardBill().getId(),
				savedTransaction.getName(),
				savedTransaction.getDescription(),
				savedTransaction.getDate(),
				savedTransaction.isInstallmentPurchase(),
				savedTransaction.getInstallmentCount(),
				savedTransaction.getPrice(),
				savedTransaction.getInstallmentPrice(),
				savedTransaction.getInstallmentNumber());
	}

	@Transactional
	public void deleteTransaction(Long id) {
		Transaction transaction = transactionRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Transaction ID not found"));
		CreditCardBill bill = transaction.getCreditCardBill();
		double installmentPrice = transaction.getInstallmentPrice() == null ? 0.0 : transaction.getInstallmentPrice();
		updateBillTotalAmount(bill, -installmentPrice);
		transactionRepository.delete(transaction);
	}

	private Transaction createTransaction(TransactionInsertDTO dto, CreditCardBill bill, Double installmentPrice,
			Integer installmentNumber) {
		Transaction transaction = new Transaction();
		transaction.setCreditCardBill(bill);
		transaction.setName(dto.name());
		transaction.setDescription(dto.description());
		transaction.setDate(dto.date());
		transaction.setInstallmentPurchase(dto.installmentPurchase());
		transaction.setInstallmentCount(dto.installmentCount());
		transaction.setPrice(dto.price());
		transaction.setInstallmentPrice(installmentPrice);
		transaction.setInstallmentNumber(installmentNumber);
		return transaction;
	}

	private void updateBillTotalAmount(CreditCardBill bill, Double amountToAdd) {
		double currentAmount = bill.getTotalAmount() == null ? 0.0 : bill.getTotalAmount();
		bill.setTotalAmount(currentAmount + amountToAdd);
	}
}
