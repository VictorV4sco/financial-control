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

import com.financial_control.dtos.CreditCardBillInsertDTO;
import com.financial_control.dtos.CreditCardBillReadDTO;
import com.financial_control.dtos.CreditCardBillUpdateDTO;
import com.financial_control.services.CreditCardBillService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/credit-card-bill")
public class CreditCardBillController {

	@Autowired
	private CreditCardBillService creditCardBillService;

	@GetMapping
	public ResponseEntity<List<CreditCardBillReadDTO>> findByCreditCardAndMonthAndYear(
			@RequestParam(required = true) Long creditCardId,
			@RequestParam(required = true) Integer year,
			@RequestParam(required = true) Integer month) {
		return new ResponseEntity<>(
				creditCardBillService.findByCreditCardAndMonthAndYear(creditCardId, year, month),
				HttpStatus.OK);
	}

	@PostMapping(value = "/insert")
	public ResponseEntity<CreditCardBillInsertDTO> insertCreditCardBill(@Valid @RequestBody CreditCardBillInsertDTO dto) {
		return new ResponseEntity<>(creditCardBillService.insertCreditCardBill(dto), HttpStatus.CREATED);
	}

	@PutMapping(value = "/update/{id}")
	public ResponseEntity<CreditCardBillUpdateDTO> updateCreditCardBill(
			@PathVariable Long id,
			@Valid @RequestBody CreditCardBillInsertDTO dto) {
		return new ResponseEntity<>(creditCardBillService.updateCreditCardBill(id, dto), HttpStatus.OK);
	}

	@DeleteMapping(value = "/delete/{id}", produces = "application/json")
	public ResponseEntity<Void> deleteCreditCardBill(@PathVariable Long id) {
		creditCardBillService.deleteCreditCardBill(id);
		return ResponseEntity.noContent().build();
	}
}
