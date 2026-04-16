# Financial Control API

## 📌 Description

A personal financial control API designed to manage income, expenses, and financial operations in a structured way.

## 🎯 Objective

This project is being developed for study and portfolio purposes, focusing on best practices in backend development using Java and Spring Boot.

## 🚧 Status

In development

## 🛠 Technologies

* Java 21
* Spring Boot 4.0.5
* Maven
* MySQL

## 🚀 Features

* User CRUD
* Accounts Payable CRUD
* Accounts Receivable CRUD
* Financial categorization
* Basic financial reports (planned)
* Authentication and authorization (planned)

## ▶️ Running the Application

To run the project locally, use the standard Maven command:

```bash
./mvnw spring-boot:run
```

Or, if you have Maven installed globally:

```bash
mvn spring-boot:run
```

The application will start on:

```
http://localhost:8080
```

## 🗄 Database

This project uses MySQL. You will need to:

1. Create a database
2. Configure your credentials in:

```
application.properties
```

Example:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/financial_db
spring.datasource.username=your_user
spring.datasource.password=your_password
```

## 📁 Project Structure (planned)

* `controller` → API endpoints
* `service` → Business logic
* `repository` → Data access layer
* `entity` → Domain models

## 📌 Future Improvements

* JWT authentication
* Docker support
* Unit and integration tests
* API documentation (Swagger/OpenAPI)

## 🤝 Contribution

This is a personal project for study and portfolio purposes, but suggestions are welcome.

## 📄 License

This project is licensed under the MIT License.
