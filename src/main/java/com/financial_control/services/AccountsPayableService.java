package com.financial_control.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.financial_control.dtos.AccountsPayableReadDTO;
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
}
