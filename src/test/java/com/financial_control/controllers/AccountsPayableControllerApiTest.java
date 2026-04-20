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
import com.financial_control.dtos.AccountsPayableInsertDTO;
import com.financial_control.dtos.AccountsPayableReadDTO;
import com.financial_control.dtos.AccountsPayableUpdateDTO;
import com.financial_control.enums.PaymentStatus;
import com.financial_control.services.AccountsPayableService;
import com.financial_control.services.exceptions.DatabaseException;
import com.financial_control.services.exceptions.ResourceNotFoundException;

import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;

@WebMvcTest(AccountsPayableController.class)
@Import(ResourceExceptionHandler.class)
class AccountsPayableControllerApiTest {

	@Autowired
	private WebApplicationContext webApplicationContext;

	@MockitoBean
	private AccountsPayableService accountsPayableService;

	@BeforeEach
	void setUp() {
		RestAssuredMockMvc.webAppContextSetup(webApplicationContext);
	}

	@AfterEach
	void tearDown() {
		RestAssuredMockMvc.reset();
	}

	@Test
	void getByMonthAndYearShouldReturnStatus200AndPayloadWhenServiceFindsResults() {
		List<AccountsPayableReadDTO> response = List.of(
				new AccountsPayableReadDTO(1L, "Internet", 150.0, LocalDate.of(2026, 4, 10), PaymentStatus.PENDING));

		Mockito.when(accountsPayableService.getByMonthAndYear(4, 2026)).thenReturn(response);

		given()
				.accept(ContentType.JSON)
				.queryParam("month", 4)
				.queryParam("year", 2026)
		.when()
				.get("/accounts-payable")
		.then()
				.statusCode(200)
				.body("$", hasSize(1))
				.body("[0].id", equalTo(1))
				.body("[0].description", equalTo("Internet"))
				.body("[0].amount", equalTo(150.0F))
				.body("[0].status", equalTo("PENDING"));
	}

	@Test
	void getByMonthAndYearShouldReturnStatus404WhenServiceThrowsResourceNotFoundException() {
		Mockito.when(accountsPayableService.getByMonthAndYear(4, 2026))
				.thenThrow(new ResourceNotFoundException("No accounts payable found for month 4 and year 2026"));

		given()
				.accept(ContentType.JSON)
				.queryParam("month", 4)
				.queryParam("year", 2026)
		.when()
				.get("/accounts-payable")
		.then()
				.statusCode(404)
				.body("error", equalTo("Resource not found"))
				.body("message", equalTo("No accounts payable found for month 4 and year 2026"))
				.body("path", equalTo("/accounts-payable"));
	}

	@Test
	void insertAccountPayableShouldReturnStatus201AndCreatedPayload() {
		String requestBody = """
				{
				  "description": "Water",
				  "amount": 89.9,
				  "dueDate": "2026-04-30"
				}
				""";

		AccountsPayableInsertDTO response = new AccountsPayableInsertDTO(
				1L, "Water", 89.9, LocalDate.of(2026, 4, 30), PaymentStatus.PENDING);

		Mockito.when(accountsPayableService.insertAccountPayable(Mockito.any(AccountsPayableInsertDTO.class)))
				.thenReturn(response);

		given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.body(requestBody)
		.when()
				.post("/accounts-payable/insert")
		.then()
				.statusCode(201)
				.body("id", equalTo(1))
				.body("description", equalTo("Water"))
				.body("amount", equalTo(89.9F))
				.body("status", equalTo("PENDING"));
	}

	@Test
	void insertAccountPayableShouldReturnStatus422WhenRequestBodyIsInvalid() {
		String requestBody = """
				{
				  "description": "",
				  "amount": -10,
				  "dueDate": "2026-01-01"
				}
				""";

		given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.body(requestBody)
		.when()
				.post("/accounts-payable/insert")
		.then()
				.statusCode(422)
				.body("error", equalTo("Validation exception"))
				.body("message", equalTo("One or more fields are invalid"))
				.body("errors.fieldName", hasItem("description"))
				.body("errors.fieldName", hasItem("amount"));
	}

	@Test
	void updateAccountPayableShouldReturnStatus200AndUpdatedPayload() {
		String requestBody = """
				{
				  "description": "Rent",
				  "amount": 1200.0,
				  "dueDate": "2026-05-10",
				  "status": "PAID"
				}
				""";

		AccountsPayableUpdateDTO response = new AccountsPayableUpdateDTO(
				1L, "Rent", 1200.0, LocalDate.of(2026, 5, 10), PaymentStatus.PAID);

		Mockito.when(accountsPayableService.updateAccountPayable(Mockito.eq(1L), Mockito.any(AccountsPayableInsertDTO.class)))
				.thenReturn(response);

		given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.body(requestBody)
		.when()
				.put("/accounts-payable/update/{id}", 1L)
		.then()
				.statusCode(200)
				.body("id", equalTo(1))
				.body("description", equalTo("Rent"))
				.body("status", equalTo("PAID"));
	}

	@Test
	void deleteAccountShouldReturnStatus204WhenDeletionSucceeds() {
		Mockito.doNothing().when(accountsPayableService).delete(1L);

		given()
				.accept(ContentType.JSON)
		.when()
				.delete("/accounts-payable/delete/{id}", 1L)
		.then()
				.statusCode(204);
	}

	@Test
	void deleteAccountShouldReturnStatus400WhenServiceThrowsDatabaseException() {
		Mockito.doThrow(new DatabaseException("Falha de integridade referencial"))
				.when(accountsPayableService).delete(1L);

		given()
				.accept(ContentType.JSON)
		.when()
				.delete("/accounts-payable/delete/{id}", 1L)
		.then()
				.statusCode(400)
				.body("error", equalTo("Database exception"))
				.body("message", equalTo("Falha de integridade referencial"))
				.body("path", equalTo("/accounts-payable/delete/1"));
	}

	@Test
	void insertAccountPayableShouldReturnStatus400WhenJsonIsMalformed() {
		String invalidJson = """
				{
				  "description": "Water",
				  "amount": 89.9,
				  "dueDate": "2026-04-30",
				}
				""";

		given()
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.accept(ContentType.JSON)
				.body(invalidJson)
		.when()
				.post("/accounts-payable/insert")
		.then()
				.statusCode(400)
				.body("error", equalTo("Malformed JSON"))
				.body("message", equalTo("Invalid request body."))
				.body("path", equalTo("/accounts-payable/insert"));
	}
}
