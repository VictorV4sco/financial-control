# Financial Control API

## Overview

Financial Control API is a personal backend project built with Java and Spring Boot to manage:

- accounts payable
- credit cards
- credit card bills
- credit card transactions

The project is being developed with a strong focus on clean layering, business rules, and automated tests.

## Current Status

The API is under active development.

Implemented modules:

- Accounts Payable CRUD
- Credit Card CRUD
- Credit Card Bill CRUD
- Transaction CRUD
- Unit, integration, API, and flow tests
- JaCoCo coverage report

## Tech Stack

- Java 21
- Spring Boot 4.0.5
- Spring Data JPA
- Spring Validation
- Spring Web MVC
- Maven
- MySQL
- H2 for tests
- JUnit 5
- Mockito
- Rest Assured Mock MVC
- JaCoCo

## Domain Model

### AccountsPayable

Represents a bill to be paid by the user.

Main fields:

- `id`
- `description`
- `amount`
- `dueDate`
- `status`

### CreditCard

Represents a credit card created by the user.

Main fields:

- `id`
- `name`

### CreditCardBill

Represents a credit card bill linked to a specific card.

Main fields:

- `id`
- `creditCard`
- `openingDate`
- `closingDate`
- `dueDate`
- `totalAmount`
- `status`

### Transaction

Represents a purchase made on a credit card bill.

Main fields:

- `id`
- `creditCardBill`
- `name`
- `description`
- `date`
- `installmentPurchase`
- `installmentCount`
- `price`
- `installmentPrice`
- `installmentNumber`

## Payment Status

The project currently uses the following payment statuses:

- `PENDING`
- `PAID`
- `OVERDUE`

## Implemented Business Rules

### Accounts Payable

- New accounts payable entries are created with status `PENDING`
- Query by month and year updates overdue bills automatically before returning results

### Credit Card Bills

- New bills are created with:
  - `totalAmount = 0.0`
  - `status = PENDING`
- Bill dates can only be changed if the bill has no transactions
- If a bill has transactions, only non-date fields such as `status` can be updated
- `totalAmount` is not manually editable through update requests
- A bill can only be deleted if it is empty

### Transactions

- Non-installment purchases are added to the selected bill
- Installment purchases are distributed across the current bill and future bills
- If future bills do not exist yet, they are created automatically
- Each installment updates the target bill `totalAmount` automatically
- Deleting a transaction updates the bill `totalAmount` automatically

### Credit Cards

- A credit card cannot be deleted if it has at least one open bill
- Open bill means any bill with status different from `PAID`
- If all bills are paid, deleting the card removes its bills and related transactions

## API Endpoints

### Accounts Payable

- `GET /accounts-payable?month={month}&year={year}`
- `POST /accounts-payable/insert`
- `PUT /accounts-payable/update/{id}`
- `DELETE /accounts-payable/delete/{id}`

### Credit Card

- `GET /credit-card`
- `POST /credit-card/insert`
- `PUT /credit-card/update/{id}`
- `DELETE /credit-card/delete/{id}`

### Credit Card Bill

- `GET /credit-card-bill?creditCardId={id}&year={year}&month={month}`
- `POST /credit-card-bill/insert`
- `PUT /credit-card-bill/update/{id}`
- `DELETE /credit-card-bill/delete/{id}`

### Transaction

- `GET /transaction?creditCardBillId={id}`
- `POST /transaction/insert`
- `PUT /transaction/update/{id}`
- `DELETE /transaction/delete/{id}`

## Running the Application

### Requirements

- Java 21
- MySQL running locally

### Application properties

The main application uses MySQL and reads configuration from:

- `src/main/resources/application.properties`

Default local configuration:

```properties
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/financial_control}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

Set the environment variables before running the API.

PowerShell example:

```powershell
$env:DB_USERNAME="your_username"
$env:DB_PASSWORD="your_password"
```

Optional, if you want to override the default URL:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/financial_control"
```

### Run with Maven Wrapper

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

The API starts at:

```text
http://localhost:8080
```

## Test Configuration

Tests use a dedicated H2 in-memory database configured in:

- `src/test/resources/application-test.properties`

This keeps test execution isolated from the MySQL development database.

## Running Tests

Run all tests:

```bash
./mvnw test
```

On Windows:

```bash
mvnw.cmd test
```

## Coverage Report

JaCoCo is configured in the Maven profile `coverage`.

Generate the report with:

```bash
./mvnw clean verify -Pcoverage
```

On Windows:

```bash
mvnw.cmd clean verify -Pcoverage
```

Generated report:

```text
target/site/jacoco/index.html
```

Current JaCoCo configuration excludes:

- `entities`
- `FinancialControlApplication`

## Project Structure

```text
src/main/java/com/financial_control
|- controllers
|- dtos
|- entities
|- enums
|- repositories
|- services
|- services/exceptions

src/test/java/com/financial_control
|- controllers
|- controllers/flow
|- services
|- services/... packages by module
```

## Testing Strategy Used In The Project

The project currently follows a layered testing approach:

- unit tests for service rules
- integration tests for service + repository behavior
- API tests for controller contracts
- flow tests joining related modules together

This helps validate both isolated business logic and real module interaction.

## Next Possible Improvements

- frontend-oriented query endpoints for bills by year and month
- better support for early payment of installments
- authentication and authorization
- Swagger / OpenAPI documentation
- Docker support

## Purpose

This is a study and portfolio project focused on backend architecture, business rules, and test coverage.
