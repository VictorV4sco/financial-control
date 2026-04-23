package com.financial_control.controllers.flow;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.WebApplicationContext;

import com.financial_control.repositories.CreditCardBillRepository;
import com.financial_control.repositories.CreditCardRepository;
import com.financial_control.repositories.TransactionRepository;

import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;

@SpringBootTest
@ActiveProfiles("test")
class CreditCardBillApiFlowIT {

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	private CreditCardRepository creditCardRepository;

	@Autowired
	private CreditCardBillRepository creditCardBillRepository;

	@Autowired
	private TransactionRepository transactionRepository;

	@BeforeEach
	void setUp() {
		transactionRepository.deleteAll();
		creditCardBillRepository.deleteAll();
		creditCardRepository.deleteAll();
		RestAssuredMockMvc.webAppContextSetup(webApplicationContext);
	}

	@AfterEach
	void tearDown() {
		RestAssuredMockMvc.reset();
	}

	@Test
	void shouldCreateCreditCardThenCreateBillThenQueryBillByCardMonthAndYear() {
		String createCardBody = """
				{
				  "name": "Nubank"
				}
				""";

		Integer creditCardId = given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.body(createCardBody)
		.when()
				.post("/credit-card/insert")
		.then()
				.statusCode(201)
				.body("name", equalTo("Nubank"))
				.extract()
				.path("id");

		String createBillBody = """
				{
				  "creditCardId": %d,
				  "openingDate": "2026-04-01",
				  "closingDate": "2026-04-25",
				  "dueDate": "2026-05-05"
				}
				""".formatted(creditCardId);

		given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.body(createBillBody)
		.when()
				.post("/credit-card-bill/insert")
		.then()
				.statusCode(201)
				.body("creditCardId", equalTo(creditCardId))
				.body("totalAmount", equalTo(0.0F))
				.body("status", equalTo("PENDING"));

		given()
				.accept(ContentType.JSON)
				.queryParam("creditCardId", creditCardId)
				.queryParam("year", 2026)
				.queryParam("month", 4)
		.when()
				.get("/credit-card-bill")
		.then()
				.statusCode(200)
				.body("$", hasSize(1))
				.body("[0].creditCardId", equalTo(creditCardId))
				.body("[0].openingDate", equalTo("2026-04-01"))
				.body("[0].closingDate", equalTo("2026-04-25"))
				.body("[0].dueDate", equalTo("2026-05-05"))
				.body("[0].status", equalTo("PENDING"));
	}

	@Test
	void shouldReturn404WhenQueryingBillsAfterDeletingCreditCard() {
		String createCardBody = """
				{
				  "name": "Delete Flow"
				}
				""";

		Integer creditCardId = given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.body(createCardBody)
		.when()
				.post("/credit-card/insert")
		.then()
				.statusCode(201)
				.extract()
				.path("id");

		String createBillBody = """
				{
				  "creditCardId": %d,
				  "openingDate": "%s",
				  "closingDate": "%s",
				  "dueDate": "%s"
				}
				""".formatted(
					creditCardId,
					LocalDate.now().toString(),
					LocalDate.now().plusDays(5).toString(),
					LocalDate.now().plusDays(10).toString());

		given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.body(createBillBody)
		.when()
				.post("/credit-card-bill/insert")
		.then()
				.statusCode(201);

		given()
				.accept(ContentType.JSON)
		.when()
				.delete("/credit-card/delete/{id}", creditCardId)
		.then()
				.statusCode(204);

		given()
				.accept(ContentType.JSON)
				.queryParam("creditCardId", creditCardId)
				.queryParam("year", LocalDate.now().plusDays(5).getYear())
				.queryParam("month", LocalDate.now().plusDays(5).getMonthValue())
		.when()
				.get("/credit-card-bill")
		.then()
				.statusCode(404)
				.body("error", equalTo("Resource not found"))
				.body("message", equalTo("No credit card bills found for this card, month and year"))
				.body("path", equalTo("/credit-card-bill"));
	}
}
