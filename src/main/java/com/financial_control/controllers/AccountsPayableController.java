package com.financial_control.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.financial_control.dtos.AccountsPayableDTO;
import com.financial_control.services.AccountsPayableService;

@RestController
@RequestMapping(value = "/accounts-payable")
public class AccountsPayableController {
	
	@Autowired
	private AccountsPayableService accountsPayableService;

	@GetMapping
	public ResponseEntity<List<AccountsPayableDTO>> getByMonthAndYear(
			@RequestParam Integer month,
			@RequestParam Integer year) {
		return new ResponseEntity<>(accountsPayableService.getByMonthAndYear(month, year), HttpStatus.OK);
	}
}
