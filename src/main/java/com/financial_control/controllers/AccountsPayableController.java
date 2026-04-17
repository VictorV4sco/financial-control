package com.financial_control.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.financial_control.dtos.AccountsPayableInsertDTO;
import com.financial_control.dtos.AccountsPayableReadDTO;
import com.financial_control.dtos.AccountsPayableUpdateDTO;
import com.financial_control.services.AccountsPayableService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/accounts-payable")
public class AccountsPayableController {
	
	@Autowired
	private AccountsPayableService accountsPayableService;

	@GetMapping
	public ResponseEntity<List<AccountsPayableReadDTO>> getByMonthAndYear(
			@RequestParam(required = true) Integer month,
			@RequestParam(required = true) Integer year) {
		return new ResponseEntity<>(accountsPayableService.getByMonthAndYear(month, year), HttpStatus.OK);
	}
	
	@PostMapping(value = "/insert")
	public ResponseEntity<AccountsPayableInsertDTO> insertAccountPayable(@Valid @RequestBody AccountsPayableInsertDTO dto) {
		return new ResponseEntity<>(accountsPayableService.insertAccountPayable(dto), HttpStatus.CREATED);
	}
	
	@PutMapping(value = "/update/{id}")
	public ResponseEntity<AccountsPayableUpdateDTO> updateAccountPayable(@PathVariable Long id, @Valid @RequestBody AccountsPayableInsertDTO dto) {
		return new ResponseEntity<>(accountsPayableService.updateAccountPayable(id, dto), HttpStatus.OK);
	}
}
