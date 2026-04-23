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
import com.financial_control.dtos.CreditCardBillInsertDTO;
import com.financial_control.dtos.CreditCardBillReadDTO;
import com.financial_control.dtos.CreditCardBillUpdateDTO;
import com.financial_control.enums.PaymentStatus;
import com.financial_control.services.CreditCardBillService;
import com.financial_control.services.exceptions.DatabaseException;
import com.financial_control.services.exceptions.ResourceNotFoundException;

import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;

@WebMvcTest(CreditCardBillController.class)
@Import(ResourceExceptionHandler.class)
class CreditCardBillControllerApiTest {

	@Autowired
	private WebApplicationContext webApplicationContext;

	@MockitoBean
	private CreditCardBillService creditCardBillService;

	@BeforeEach
	void setUp() {
		RestAssuredMockMvc.webAppContextSetup(webApplicationContext);
	}

	@AfterEach
	void tearDown() {
		RestAssuredMockMvc.reset();
	}

	@Test
	void findByCreditCardAndMonthAndYearShouldReturnStatus200AndPayloadWhenServiceFindsResults() {
		List<CreditCardBillReadDTO> response = List.of(
				new CreditCardBillReadDTO(
						1L,
						1L,
						LocalDate.of(2026, 4, 1),
						LocalDate.of(2026, 4, 25),
						LocalDate.of(2026, 5, 5),
						0.0,
						PaymentStatus.PENDING));

		Mockito.when(creditCardBillService.findByCreditCardAndMonthAndYear(1L, 2026, 4)).thenReturn(response);

		given()
				.accept(ContentType.JSON)
				.queryParam("creditCardId", 1L)
				.queryParam("year", 2026)
				.queryParam("month", 4)
		.when()
				.get("/credit-card-bill")
		.then()
				.statusCode(200)
				.body("$", hasSize(1))
				.body("[0].creditCardId", equalTo(1))
				.body("[0].status", equalTo("PENDING"));
	}

	@Test
	void findByCreditCardAndMonthAndYearShouldReturnStatus404WhenServiceThrowsResourceNotFoundException() {
		Mockito.when(creditCardBillService.findByCreditCardAndMonthAndYear(1L, 2026, 4))
				.thenThrow(new ResourceNotFoundException("No credit card bills found for this card, month and year"));

		given()
				.accept(ContentType.JSON)
				.queryParam("creditCardId", 1L)
				.queryParam("year", 2026)
				.queryParam("month", 4)
		.when()
				.get("/credit-card-bill")
		.then()
				.statusCode(404)
				.body("error", equalTo("Resource not found"))
				.body("message", equalTo("No credit card bills found for this card, month and year"))
				.body("path", equalTo("/credit-card-bill"));
	}

	@Test
	void insertCreditCardBillShouldReturnStatus201AndCreatedPayload() {
		String requestBody = """
				{
				  "creditCardId": 1,
				  "openingDate": "2026-04-01",
				  "closingDate": "2026-04-25",
				  "dueDate": "2026-05-05"
				}
				""";

		CreditCardBillInsertDTO response = new CreditCardBillInsertDTO(
				10L,
				1L,
				LocalDate.of(2026, 4, 1),
				LocalDate.of(2026, 4, 25),
				LocalDate.of(2026, 5, 5),
				0.0,
				PaymentStatus.PENDING);

		Mockito.when(creditCardBillService.insertCreditCardBill(Mockito.any(CreditCardBillInsertDTO.class)))
				.thenReturn(response);

		given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.body(requestBody)
		.when()
				.post("/credit-card-bill/insert")
		.then()
				.statusCode(201)
				.body("id", equalTo(10))
				.body("creditCardId", equalTo(1))
				.body("totalAmount", equalTo(0.0F))
				.body("status", equalTo("PENDING"));
	}

	@Test
	void insertCreditCardBillShouldReturnStatus422WhenRequestBodyIsInvalid() {
		String requestBody = """
				{
				  "creditCardId": 0,
				  "openingDate": null,
				  "closingDate": "2026-04-25",
				  "dueDate": "2026-05-05"
				}
				""";

		given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.body(requestBody)
		.when()
				.post("/credit-card-bill/insert")
		.then()
				.statusCode(422)
				.body("error", equalTo("Validation exception"))
				.body("message", equalTo("One or more fields are invalid"))
				.body("errors.fieldName", hasItem("creditCardId"))
				.body("errors.fieldName", hasItem("openingDate"));
	}

	@Test
	void insertCreditCardBillShouldReturnStatus404WhenServiceThrowsResourceNotFoundException() {
		String requestBody = """
				{
				  "creditCardId": 1,
				  "openingDate": "2026-04-01",
				  "closingDate": "2026-04-25",
				  "dueDate": "2026-05-05"
				}
				""";

		Mockito.when(creditCardBillService.insertCreditCardBill(Mockito.any(CreditCardBillInsertDTO.class)))
				.thenThrow(new ResourceNotFoundException("Credit card ID not found"));

		given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.body(requestBody)
		.when()
				.post("/credit-card-bill/insert")
		.then()
				.statusCode(404)
				.body("error", equalTo("Resource not found"))
				.body("message", equalTo("Credit card ID not found"))
				.body("path", equalTo("/credit-card-bill/insert"));
	}

	@Test
	void updateCreditCardBillShouldReturnStatus200AndUpdatedPayload() {
		String requestBody = """
				{
				  "creditCardId": 1,
				  "openingDate": "2026-04-25",
				  "closingDate": "2026-05-25",
				  "dueDate": "2026-06-05",
				  "totalAmount": 250.0,
				  "status": "PAID"
				}
				""";

		CreditCardBillUpdateDTO response = new CreditCardBillUpdateDTO(
				10L,
				1L,
				LocalDate.of(2026, 4, 25),
				LocalDate.of(2026, 5, 25),
				LocalDate.of(2026, 6, 5),
				1000.0,
				PaymentStatus.PAID);

		Mockito.when(creditCardBillService.updateCreditCardBill(Mockito.eq(10L), Mockito.any(CreditCardBillInsertDTO.class)))
				.thenReturn(response);

		given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.body(requestBody)
		.when()
				.put("/credit-card-bill/update/{id}", 10L)
		.then()
				.statusCode(200)
				.body("id", equalTo(10))
				.body("creditCardId", equalTo(1))
				.body("totalAmount", equalTo(1000.0F))
				.body("status", equalTo("PAID"));
	}

	@Test
	void updateCreditCardBillShouldReturnStatus404WhenServiceThrowsResourceNotFoundException() {
		String requestBody = """
				{
				  "creditCardId": 1,
				  "openingDate": "2026-04-25",
				  "closingDate": "2026-05-25",
				  "dueDate": "2026-06-05",
				  "totalAmount": 250.0,
				  "status": "PAID"
				}
				""";

		Mockito.when(creditCardBillService.updateCreditCardBill(Mockito.eq(10L), Mockito.any(CreditCardBillInsertDTO.class)))
				.thenThrow(new ResourceNotFoundException("Credit card bill ID not found"));

		given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.body(requestBody)
		.when()
				.put("/credit-card-bill/update/{id}", 10L)
		.then()
				.statusCode(404)
				.body("error", equalTo("Resource not found"))
				.body("message", equalTo("Credit card bill ID not found"))
				.body("path", equalTo("/credit-card-bill/update/10"));
	}

	@Test
	void updateCreditCardBillShouldReturnStatus400WhenChangingDatesAndBillHasTransactions() {
		String requestBody = """
				{
				  "creditCardId": 1,
				  "openingDate": "2026-04-25",
				  "closingDate": "2026-05-25",
				  "dueDate": "2026-06-05",
				  "status": "PAID"
				}
				""";

		Mockito.when(creditCardBillService.updateCreditCardBill(Mockito.eq(10L), Mockito.any(CreditCardBillInsertDTO.class)))
				.thenThrow(new DatabaseException("Credit card bill dates cannot be changed because it has transactions"));

		given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.body(requestBody)
		.when()
				.put("/credit-card-bill/update/{id}", 10L)
		.then()
				.statusCode(400)
				.body("error", equalTo("Database exception"))
				.body("message", equalTo("Credit card bill dates cannot be changed because it has transactions"))
				.body("path", equalTo("/credit-card-bill/update/10"));
	}

	@Test
	void deleteCreditCardBillShouldReturnStatus204WhenDeletionSucceeds() {
		Mockito.doNothing().when(creditCardBillService).deleteCreditCardBill(10L);

		given()
				.accept(ContentType.JSON)
		.when()
				.delete("/credit-card-bill/delete/{id}", 10L)
		.then()
				.statusCode(204);
	}

	@Test
	void deleteCreditCardBillShouldReturnStatus404WhenServiceThrowsResourceNotFoundException() {
		Mockito.doThrow(new ResourceNotFoundException("Credit card bill ID not found"))
				.when(creditCardBillService).deleteCreditCardBill(10L);

		given()
				.accept(ContentType.JSON)
		.when()
				.delete("/credit-card-bill/delete/{id}", 10L)
		.then()
				.statusCode(404)
				.body("error", equalTo("Resource not found"))
				.body("message", equalTo("Credit card bill ID not found"))
				.body("path", equalTo("/credit-card-bill/delete/10"));
	}

	@Test
	void deleteCreditCardBillShouldReturnStatus400WhenBillHasTransactions() {
		Mockito.doThrow(new DatabaseException("Credit card bill cannot be deleted because it has transactions"))
				.when(creditCardBillService).deleteCreditCardBill(10L);

		given()
				.accept(ContentType.JSON)
		.when()
				.delete("/credit-card-bill/delete/{id}", 10L)
		.then()
				.statusCode(400)
				.body("error", equalTo("Database exception"))
				.body("message", equalTo("Credit card bill cannot be deleted because it has transactions"))
				.body("path", equalTo("/credit-card-bill/delete/10"));
	}

	@Test
	void insertCreditCardBillShouldReturnStatus400WhenJsonIsMalformed() {
		String invalidJson = """
				{
				  "creditCardId": 1,
				  "openingDate": "2026-04-01",
				  "closingDate": "2026-04-25",
				  "dueDate": "2026-05-05",
				}
				""";

		given()
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.accept(ContentType.JSON)
				.body(invalidJson)
		.when()
				.post("/credit-card-bill/insert")
		.then()
				.statusCode(400)
				.body("error", equalTo("Malformed JSON"))
				.body("message", equalTo("Invalid request body."))
				.body("path", equalTo("/credit-card-bill/insert"));
	}
}
