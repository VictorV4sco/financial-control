package com.financial_control.controllers;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.WebApplicationContext;

import com.financial_control.controllers.exceptions.ResourceExceptionHandler;
import com.financial_control.dtos.TransactionInsertDTO;
import com.financial_control.dtos.TransactionReadDTO;
import com.financial_control.dtos.TransactionUpdateDTO;
import com.financial_control.services.TransactionService;
import com.financial_control.services.exceptions.ResourceNotFoundException;

import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;

@WebMvcTest(TransactionController.class)
@Import(ResourceExceptionHandler.class)
class TransactionControllerApiTest {

	@Autowired
	private WebApplicationContext webApplicationContext;

	@MockitoBean
	private TransactionService transactionService;

	@BeforeEach
	void setUp() {
		RestAssuredMockMvc.webAppContextSetup(webApplicationContext);
	}

	@AfterEach
	void tearDown() {
		RestAssuredMockMvc.reset();
	}

	@Test
	void findByCreditCardBillShouldReturnStatus200AndPayloadWhenServiceFindsResults() {
		List<TransactionReadDTO> response = List.of(
				new TransactionReadDTO(
						1L,
						1L,
						"Notebook",
						"Work purchase",
						LocalDate.of(2026, 4, 15),
						true,
						3,
						3000.0,
						1000.0,
						1));

		Mockito.when(transactionService.findByCreditCardBill(1L)).thenReturn(response);

		given()
				.accept(ContentType.JSON)
				.queryParam("creditCardBillId", 1L)
		.when()
				.get("/transaction")
		.then()
				.statusCode(200)
				.body("$", hasSize(1))
				.body("[0].creditCardBillId", equalTo(1))
				.body("[0].name", equalTo("Notebook"))
				.body("[0].installmentNumber", equalTo(1));
	}

	@Test
	void findByCreditCardBillShouldReturnStatus404WhenServiceThrowsResourceNotFoundException() {
		Mockito.when(transactionService.findByCreditCardBill(1L))
				.thenThrow(new ResourceNotFoundException("No transactions found for this bill"));

		given()
				.accept(ContentType.JSON)
				.queryParam("creditCardBillId", 1L)
		.when()
				.get("/transaction")
		.then()
				.statusCode(404)
				.body("error", equalTo("Resource not found"))
				.body("message", equalTo("No transactions found for this bill"))
				.body("path", equalTo("/transaction"));
	}

	@Test
	void insertTransactionShouldReturnStatus201AndCreatedPayload() {
		String requestBody = """
				{
				  "creditCardBillId": 1,
				  "name": "Headphone",
				  "description": "Bluetooth headphone",
				  "date": "2026-04-15",
				  "installmentPurchase": false,
				  "installmentCount": 1,
				  "price": 600.0
				}
				""";

		TransactionInsertDTO response = new TransactionInsertDTO(
				10L,
				1L,
				"Headphone",
				"Bluetooth headphone",
				LocalDate.of(2026, 4, 15),
				false,
				1,
				600.0);

		Mockito.when(transactionService.insertTransaction(Mockito.any(TransactionInsertDTO.class)))
				.thenReturn(response);

		given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.body(requestBody)
		.when()
				.post("/transaction/insert")
		.then()
				.statusCode(201)
				.body("id", equalTo(10))
				.body("creditCardBillId", equalTo(1))
				.body("name", equalTo("Headphone"))
				.body("price", equalTo(600.0F));
	}

	@Test
	void insertTransactionShouldReturnStatus422WhenRequestBodyIsInvalid() {
		String requestBody = """
				{
				  "creditCardBillId": null,
				  "name": "",
				  "description": "Bluetooth headphone",
				  "date": null,
				  "installmentPurchase": false,
				  "installmentCount": 0,
				  "price": null
				}
				""";

		given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.body(requestBody)
		.when()
				.post("/transaction/insert")
		.then()
				.statusCode(422)
				.body("error", equalTo("Validation exception"))
				.body("message", equalTo("One or more fields are invalid"))
				.body("errors.fieldName", hasItem("creditCardBillId"))
				.body("errors.fieldName", hasItem("name"))
				.body("errors.fieldName", hasItem("date"))
				.body("errors.fieldName", hasItem("installmentCount"))
				.body("errors.fieldName", hasItem("price"));
	}

	@Test
	void insertTransactionShouldReturnStatus404WhenServiceThrowsResourceNotFoundException() {
		String requestBody = """
				{
				  "creditCardBillId": 1,
				  "name": "Headphone",
				  "description": "Bluetooth headphone",
				  "date": "2026-04-15",
				  "installmentPurchase": false,
				  "installmentCount": 1,
				  "price": 600.0
				}
				""";

		Mockito.when(transactionService.insertTransaction(Mockito.any(TransactionInsertDTO.class)))
				.thenThrow(new ResourceNotFoundException("Credit card bill ID not found"));

		given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.body(requestBody)
		.when()
				.post("/transaction/insert")
		.then()
				.statusCode(404)
				.body("error", equalTo("Resource not found"))
				.body("message", equalTo("Credit card bill ID not found"))
				.body("path", equalTo("/transaction/insert"));
	}

	@Test
	void updateTransactionShouldReturnStatus200AndUpdatedPayload() {
		String requestBody = """
				{
				  "creditCardBillId": 1,
				  "name": "Mouse Updated",
				  "description": "Updated description",
				  "date": "2026-04-15",
				  "installmentPurchase": false,
				  "installmentCount": 1,
				  "price": 250.0
				}
				""";

		TransactionUpdateDTO response = new TransactionUpdateDTO(
				1L,
				1L,
				"Mouse Updated",
				"Updated description",
				LocalDate.of(2026, 4, 15),
				false,
				1,
				250.0,
				250.0,
				1);

		Mockito.when(transactionService.updateTransaction(Mockito.eq(1L), Mockito.any(TransactionInsertDTO.class)))
				.thenReturn(response);

		given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.body(requestBody)
		.when()
				.put("/transaction/update/{id}", 1L)
		.then()
				.statusCode(200)
				.body("id", equalTo(1))
				.body("name", equalTo("Mouse Updated"))
				.body("installmentPrice", equalTo(250.0F));
	}

	@Test
	void updateTransactionShouldReturnStatus404WhenServiceThrowsResourceNotFoundException() {
		String requestBody = """
				{
				  "creditCardBillId": 1,
				  "name": "Mouse Updated",
				  "description": "Updated description",
				  "date": "2026-04-15",
				  "installmentPurchase": false,
				  "installmentCount": 1,
				  "price": 250.0
				}
				""";

		Mockito.when(transactionService.updateTransaction(Mockito.eq(1L), Mockito.any(TransactionInsertDTO.class)))
				.thenThrow(new ResourceNotFoundException("Transaction ID not found"));

		given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.body(requestBody)
		.when()
				.put("/transaction/update/{id}", 1L)
		.then()
				.statusCode(404)
				.body("error", equalTo("Resource not found"))
				.body("message", equalTo("Transaction ID not found"))
				.body("path", equalTo("/transaction/update/1"));
	}

	@Test
	void deleteTransactionShouldReturnStatus204WhenDeletionSucceeds() {
		Mockito.doNothing().when(transactionService).deleteTransaction(1L);

		given()
				.accept(ContentType.JSON)
		.when()
				.delete("/transaction/delete/{id}", 1L)
		.then()
				.statusCode(204);
	}

	@Test
	void deleteTransactionShouldReturnStatus404WhenServiceThrowsResourceNotFoundException() {
		Mockito.doThrow(new ResourceNotFoundException("Transaction ID not found"))
				.when(transactionService).deleteTransaction(1L);

		given()
				.accept(ContentType.JSON)
		.when()
				.delete("/transaction/delete/{id}", 1L)
		.then()
				.statusCode(404)
				.body("error", equalTo("Resource not found"))
				.body("message", equalTo("Transaction ID not found"))
				.body("path", equalTo("/transaction/delete/1"));
	}

	@Test
	void insertTransactionShouldReturnStatus400WhenJsonIsMalformed() {
		String invalidJson = """
				{
				  "creditCardBillId": 1,
				  "name": "Headphone",
				  "description": "Bluetooth headphone",
				  "date": "2026-04-15",
				  "installmentPurchase": false,
				  "installmentCount": 1,
				  "price": 600.0,
				}
				""";

		given()
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.accept(ContentType.JSON)
				.body(invalidJson)
		.when()
				.post("/transaction/insert")
		.then()
				.statusCode(400)
				.body("error", equalTo("Malformed JSON"))
				.body("message", equalTo("Invalid request body."))
				.body("path", equalTo("/transaction/insert"));
	}
}
