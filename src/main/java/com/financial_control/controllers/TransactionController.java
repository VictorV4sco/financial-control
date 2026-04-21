package com.financial_control.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.financial_control.dtos.TransactionInsertDTO;
import com.financial_control.dtos.TransactionReadDTO;
import com.financial_control.dtos.TransactionUpdateDTO;
import com.financial_control.services.TransactionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/transaction")
public class TransactionController {

	@Autowired
	private TransactionService transactionService;

	@GetMapping
	public ResponseEntity<List<TransactionReadDTO>> findByCreditCardBill(
			@RequestParam(required = true) Long creditCardBillId) {
		return new ResponseEntity<>(transactionService.findByCreditCardBill(creditCardBillId), HttpStatus.OK);
	}

	@PostMapping(value = "/insert")
	public ResponseEntity<TransactionInsertDTO> insertTransaction(@Valid @RequestBody TransactionInsertDTO dto) {
		return new ResponseEntity<>(transactionService.insertTransaction(dto), HttpStatus.CREATED);
	}

	@PutMapping(value = "/update/{id}")
	public ResponseEntity<TransactionUpdateDTO> updateTransaction(
			@PathVariable Long id,
			@Valid @RequestBody TransactionInsertDTO dto) {
		return new ResponseEntity<>(transactionService.updateTransaction(id, dto), HttpStatus.OK);
	}

	@DeleteMapping(value = "/delete/{id}", produces = "application/json")
	public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
		transactionService.deleteTransaction(id);
		return ResponseEntity.noContent().build();
	}
}
