package com.financial_control.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.financial_control.dtos.AccountsPayableInsertDTO;
import com.financial_control.dtos.AccountsPayableReadDTO;
import com.financial_control.services.AccountsPayableService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/accounts-payable")
public class AccountsPayableController {
	
	@Autowired
	private AccountsPayableService accountsPayableService;

	@GetMapping
	public ResponseEntity<List<AccountsPayableReadDTO>> getByMonthAndYear(
			@RequestParam Integer month,
			@RequestParam Integer year) {
		return new ResponseEntity<>(accountsPayableService.getByMonthAndYear(month, year), HttpStatus.OK);
	}
	
	@PostMapping(value = "/insert")
	public ResponseEntity<AccountsPayableInsertDTO> insertAccountPayable(@Valid @RequestBody AccountsPayableInsertDTO dto) {
		return new ResponseEntity<>(accountsPayableService.insertAccountPayable(dto), HttpStatus.CREATED);
	}
}
