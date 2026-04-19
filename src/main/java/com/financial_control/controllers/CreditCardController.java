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
import org.springframework.web.bind.annotation.RestController;

import com.financial_control.dtos.CreditCardInsertDTO;
import com.financial_control.dtos.CreditCardReadDTO;
import com.financial_control.dtos.CreditCardUpdateDTO;
import com.financial_control.services.CreditCardService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/credit-card")
public class CreditCardController {

	@Autowired
	private CreditCardService creditCardService;

	@GetMapping
	public ResponseEntity<List<CreditCardReadDTO>> findAllCreditCard() {
		return new ResponseEntity<>(creditCardService.findAllCreditCard(), HttpStatus.OK);
	}

	@PostMapping(value = "/insert")
	public ResponseEntity<CreditCardInsertDTO> insertCreditCard(@Valid @RequestBody CreditCardInsertDTO dto) {
		return new ResponseEntity<>(creditCardService.insertCreditCard(dto), HttpStatus.CREATED);
	}

	@PutMapping(value = "/update/{id}")
	public ResponseEntity<CreditCardUpdateDTO> updateCreditCard(@PathVariable Long id, @Valid @RequestBody CreditCardInsertDTO dto) {
		return new ResponseEntity<>(creditCardService.updateCreditCard(id, dto), HttpStatus.OK);
	}

	@DeleteMapping(value = "/delete/{id}", produces = "application/json")
	public ResponseEntity<Void> deleteCreditCard(@PathVariable Long id) {
		creditCardService.deleteCreditCard(id);
		return ResponseEntity.noContent().build();
	}
}
