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
class CreditCardTransactionApiFlowIT {

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	private TransactionRepository transactionRepository;

	@Autowired
	private CreditCardBillRepository creditCardBillRepository;

	@Autowired
	private CreditCardRepository creditCardRepository;

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
	void shouldCreateInstallmentTransactionAndDistributeItAcrossBills() {
		Integer creditCardId = createCreditCard("Nubank");
		Integer firstBillId = createBill(creditCardId, "2026-04-01", "2026-04-25", "2026-05-05");

		String createTransactionBody = """
				{
				  "creditCardBillId": %d,
				  "name": "Notebook",
				  "description": "Work purchase",
				  "date": "2026-04-15",
				  "installmentPurchase": true,
				  "installmentCount": 3,
				  "price": 3000.0
				}
				""".formatted(firstBillId);

		given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.body(createTransactionBody)
		.when()
				.post("/transaction/insert")
		.then()
				.statusCode(201)
				.body("creditCardBillId", equalTo(firstBillId))
				.body("name", equalTo("Notebook"))
				.body("installmentPurchase", equalTo(true))
				.body("installmentCount", equalTo(3))
				.body("price", equalTo(3000.0F));

		given()
				.accept(ContentType.JSON)
				.queryParam("creditCardBillId", firstBillId)
		.when()
				.get("/transaction")
		.then()
				.statusCode(200)
				.body("$", hasSize(1))
				.body("[0].creditCardBillId", equalTo(firstBillId))
				.body("[0].installmentPrice", equalTo(1000.0F))
				.body("[0].installmentNumber", equalTo(1));

		Integer secondBillId = given()
				.accept(ContentType.JSON)
				.queryParam("creditCardId", creditCardId)
				.queryParam("year", 2026)
				.queryParam("month", 5)
		.when()
				.get("/credit-card-bill")
		.then()
				.statusCode(200)
				.body("$", hasSize(1))
				.body("[0].openingDate", equalTo("2026-04-25"))
				.body("[0].closingDate", equalTo("2026-05-25"))
				.body("[0].dueDate", equalTo("2026-06-05"))
				.body("[0].totalAmount", equalTo(1000.0F))
				.extract()
				.path("[0].id");

		Integer thirdBillId = given()
				.accept(ContentType.JSON)
				.queryParam("creditCardId", creditCardId)
				.queryParam("year", 2026)
				.queryParam("month", 6)
		.when()
				.get("/credit-card-bill")
		.then()
				.statusCode(200)
				.body("$", hasSize(1))
				.body("[0].openingDate", equalTo("2026-05-25"))
				.body("[0].closingDate", equalTo("2026-06-25"))
				.body("[0].dueDate", equalTo("2026-07-05"))
				.body("[0].totalAmount", equalTo(1000.0F))
				.extract()
				.path("[0].id");

		given()
				.accept(ContentType.JSON)
				.queryParam("creditCardId", creditCardId)
				.queryParam("year", 2026)
				.queryParam("month", 4)
		.when()
				.get("/credit-card-bill")
		.then()
				.statusCode(200)
				.body("[0].totalAmount", equalTo(1000.0F));

		given()
				.accept(ContentType.JSON)
				.queryParam("creditCardBillId", secondBillId)
		.when()
				.get("/transaction")
		.then()
				.statusCode(200)
				.body("$", hasSize(1))
				.body("[0].creditCardBillId", equalTo(secondBillId))
				.body("[0].installmentPrice", equalTo(1000.0F))
				.body("[0].installmentNumber", equalTo(2));

		given()
				.accept(ContentType.JSON)
				.queryParam("creditCardBillId", thirdBillId)
		.when()
				.get("/transaction")
		.then()
				.statusCode(200)
				.body("$", hasSize(1))
				.body("[0].creditCardBillId", equalTo(thirdBillId))
				.body("[0].installmentPrice", equalTo(1000.0F))
				.body("[0].installmentNumber", equalTo(3));
	}

	@Test
	void shouldDeleteTransactionsWhenDeletingCreditCardByApi() {
		Integer creditCardId = createCreditCard("Delete Flow");
		Integer firstBillId = createBill(creditCardId, "2026-04-01", "2026-04-25", "2026-05-05");

		String createTransactionBody = """
				{
				  "creditCardBillId": %d,
				  "name": "Notebook",
				  "description": "Work purchase",
				  "date": "2026-04-15",
				  "installmentPurchase": true,
				  "installmentCount": 2,
				  "price": 3000.0
				}
				""".formatted(firstBillId);

		given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.body(createTransactionBody)
		.when()
				.post("/transaction/insert")
		.then()
				.statusCode(201);

		Integer secondBillId = given()
				.accept(ContentType.JSON)
				.queryParam("creditCardId", creditCardId)
				.queryParam("year", 2026)
				.queryParam("month", 5)
		.when()
				.get("/credit-card-bill")
		.then()
				.statusCode(200)
				.extract()
				.path("[0].id");

		String firstBillUpdateBody = """
				{
				  "creditCardId": %d,
				  "openingDate": "2026-04-01",
				  "closingDate": "2026-04-25",
				  "dueDate": "2026-05-05",
				  "status": "PAID"
				}
				""".formatted(creditCardId);

		String secondBillUpdateBody = """
				{
				  "creditCardId": %d,
				  "openingDate": "2026-04-25",
				  "closingDate": "2026-05-25",
				  "dueDate": "2026-06-05",
				  "status": "PAID"
				}
				""".formatted(creditCardId);

		given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.body(firstBillUpdateBody)
		.when()
				.put("/credit-card-bill/update/{id}", firstBillId)
		.then()
				.statusCode(200)
				.body("status", equalTo("PAID"));

		given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.body(secondBillUpdateBody)
		.when()
				.put("/credit-card-bill/update/{id}", secondBillId)
		.then()
				.statusCode(200)
				.body("status", equalTo("PAID"));

		given()
				.accept(ContentType.JSON)
		.when()
				.delete("/credit-card/delete/{id}", creditCardId)
		.then()
				.statusCode(204);

		given()
				.accept(ContentType.JSON)
				.queryParam("creditCardBillId", firstBillId)
		.when()
				.get("/transaction")
		.then()
				.statusCode(404)
				.body("error", equalTo("Resource not found"))
				.body("message", equalTo("No transactions found for this bill"))
				.body("path", equalTo("/transaction"));
	}

	private Integer createCreditCard(String name) {
		String createCardBody = """
				{
				  "name": "%s"
				}
				""".formatted(name);

		return given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.body(createCardBody)
		.when()
				.post("/credit-card/insert")
		.then()
				.statusCode(201)
				.extract()
				.path("id");
	}

	private Integer createBill(Integer creditCardId, String openingDate, String closingDate, String dueDate) {
		String createBillBody = """
				{
				  "creditCardId": %d,
				  "openingDate": "%s",
				  "closingDate": "%s",
				  "dueDate": "%s"
				}
				""".formatted(creditCardId, openingDate, closingDate, dueDate);

		return given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.body(createBillBody)
		.when()
				.post("/credit-card-bill/insert")
		.then()
				.statusCode(201)
				.extract()
				.path("id");
	}
}
