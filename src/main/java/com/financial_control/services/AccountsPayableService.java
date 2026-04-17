package com.financial_control.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.financial_control.dtos.AccountsPayableInsertDTO;
import com.financial_control.dtos.AccountsPayableReadDTO;
import com.financial_control.dtos.AccountsPayableUpdateDTO;
import com.financial_control.entities.AccountsPayable;
import com.financial_control.enums.PaymentStatus;
import com.financial_control.repositories.AccountsPayableRepository;
import com.financial_control.services.exceptions.ResourceNotFoundException;

@Service
public class AccountsPayableService {

	@Autowired
	private AccountsPayableRepository accountsPayableRepository;
	
	@Transactional(readOnly = true)
	public List<AccountsPayableReadDTO> getByMonthAndYear(Integer month, Integer year) {
		List<AccountsPayableReadDTO> list =
	            accountsPayableRepository.findByMonthAndYear(month, year);

	    if (list.isEmpty()) {
	        throw new ResourceNotFoundException(
	                "No accounts payable found for month " + month + " and year " + year
	        );
	    }

	    return list;
	}
	
	@Transactional
	public AccountsPayableInsertDTO insertAccountPayable(AccountsPayableInsertDTO dto) {
		AccountsPayable bill = new AccountsPayable();
		bill.setDescription(dto.description());
		bill.setAmount(dto.amount());
		bill.setDueDate(dto.dueDate());
		bill.setStatus(PaymentStatus.PENDING);
		
		AccountsPayable billSaved = accountsPayableRepository.save(bill);
		return new AccountsPayableInsertDTO(billSaved.getId(), billSaved.getDescription(), billSaved.getAmount(), billSaved.getDueDate(), billSaved.getStatus());
	}

	@Transactional
	public AccountsPayableUpdateDTO updateAccountPayable(Long id, AccountsPayableInsertDTO dto) {
		AccountsPayable bill = accountsPayableRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("ID not found"));
		bill.setDescription(dto.description());
		bill.setAmount(dto.amount());
		bill.setDueDate(dto.dueDate());
		bill.setStatus(dto.status());
		
		AccountsPayable billSaved = accountsPayableRepository.save(bill);
		return new AccountsPayableUpdateDTO(billSaved.getId(), billSaved.getDescription(), billSaved.getAmount(), billSaved.getDueDate(), billSaved.getStatus());
	}
	
}
