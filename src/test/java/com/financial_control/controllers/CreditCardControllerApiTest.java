package com.financial_control.controllers;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

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
import com.financial_control.dtos.CreditCardInsertDTO;
import com.financial_control.dtos.CreditCardReadDTO;
import com.financial_control.dtos.CreditCardUpdateDTO;
import com.financial_control.services.CreditCardService;
import com.financial_control.services.exceptions.DatabaseException;
import com.financial_control.services.exceptions.ResourceNotFoundException;

import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;

@WebMvcTest(CreditCardController.class)
@Import(ResourceExceptionHandler.class)
class CreditCardControllerApiTest {

	@Autowired
	private WebApplicationContext webApplicationContext;

	@MockitoBean
	private CreditCardService creditCardService;

	@BeforeEach
	void setUp() {
		RestAssuredMockMvc.webAppContextSetup(webApplicationContext);
	}

	@AfterEach
	void tearDown() {
		RestAssuredMockMvc.reset();
	}

	@Test
	void findAllCreditCardShouldReturnStatus200AndPayloadWhenServiceFindsResults() {
		List<CreditCardReadDTO> response = List.of(
				new CreditCardReadDTO(1L, "Nubank"),
				new CreditCardReadDTO(2L, "Inter"));

		Mockito.when(creditCardService.findAllCreditCard()).thenReturn(response);

		given()
				.accept(ContentType.JSON)
		.when()
				.get("/credit-card")
		.then()
				.statusCode(200)
				.body("$", hasSize(2))
				.body("name", hasItem("Nubank"))
				.body("name", hasItem("Inter"));
	}

	@Test
	void insertCreditCardShouldReturnStatus201AndCreatedPayload() {
		String requestBody = """
				{
				  "name": "Nubank"
				}
				""";

		CreditCardInsertDTO response = new CreditCardInsertDTO(1L, "Nubank");

		Mockito.when(creditCardService.insertCreditCard(Mockito.any(CreditCardInsertDTO.class)))
				.thenReturn(response);

		given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.body(requestBody)
		.when()
				.post("/credit-card/insert")
		.then()
				.statusCode(201)
				.body("id", equalTo(1))
				.body("name", equalTo("Nubank"));
	}

	@Test
	void insertCreditCardShouldReturnStatus422WhenRequestBodyIsInvalid() {
		String requestBody = """
				{
				  "name": ""
				}
				""";

		given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.body(requestBody)
		.when()
				.post("/credit-card/insert")
		.then()
				.statusCode(422)
				.body("error", equalTo("Validation exception"))
				.body("message", equalTo("One or more fields are invalid"))
				.body("errors.fieldName", hasItem("name"));
	}

	@Test
	void updateCreditCardShouldReturnStatus200AndUpdatedPayload() {
		String requestBody = """
				{
				  "name": "Inter Black"
				}
				""";

		CreditCardUpdateDTO response = new CreditCardUpdateDTO(1L, "Inter Black");

		Mockito.when(creditCardService.updateCreditCard(Mockito.eq(1L), Mockito.any(CreditCardInsertDTO.class)))
				.thenReturn(response);

		given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.body(requestBody)
		.when()
				.put("/credit-card/update/{id}", 1L)
		.then()
				.statusCode(200)
				.body("id", equalTo(1))
				.body("name", equalTo("Inter Black"));
	}

	@Test
	void updateCreditCardShouldReturnStatus404WhenServiceThrowsResourceNotFoundException() {
		String requestBody = """
				{
				  "name": "Inter Black"
				}
				""";

		Mockito.when(creditCardService.updateCreditCard(Mockito.eq(1L), Mockito.any(CreditCardInsertDTO.class)))
				.thenThrow(new ResourceNotFoundException("ID not found"));

		given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.body(requestBody)
		.when()
				.put("/credit-card/update/{id}", 1L)
		.then()
				.statusCode(404)
				.body("error", equalTo("Resource not found"))
				.body("message", equalTo("ID not found"))
				.body("path", equalTo("/credit-card/update/1"));
	}

	@Test
	void deleteCreditCardShouldReturnStatus204WhenDeletionSucceeds() {
		Mockito.doNothing().when(creditCardService).deleteCreditCard(1L);

		given()
				.accept(ContentType.JSON)
		.when()
				.delete("/credit-card/delete/{id}", 1L)
		.then()
				.statusCode(204);
	}

	@Test
	void deleteCreditCardShouldReturnStatus400WhenServiceThrowsDatabaseException() {
		Mockito.doThrow(new DatabaseException("Falha de integridade referencial"))
				.when(creditCardService).deleteCreditCard(1L);

		given()
				.accept(ContentType.JSON)
		.when()
				.delete("/credit-card/delete/{id}", 1L)
		.then()
				.statusCode(400)
				.body("error", equalTo("Database exception"))
				.body("message", equalTo("Falha de integridade referencial"))
				.body("path", equalTo("/credit-card/delete/1"));
	}

	@Test
	void insertCreditCardShouldReturnStatus400WhenJsonIsMalformed() {
		String invalidJson = """
				{
				  "name": "Nubank",
				}
				""";

		given()
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.accept(ContentType.JSON)
				.body(invalidJson)
		.when()
				.post("/credit-card/insert")
		.then()
				.statusCode(400)
				.body("error", equalTo("Malformed JSON"))
				.body("message", equalTo("Invalid request body."))
				.body("path", equalTo("/credit-card/insert"));
	}
}
