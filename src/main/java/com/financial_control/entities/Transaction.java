package com.financial_control.entities;

import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Transaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "credit_card_bill_id")
	private CreditCardBill creditCardBill;

	private String name;
	private String description;
	private LocalDate date;
	private boolean installmentPurchase;
	private Integer installmentCount;
	private Double price;
	private Double installmentPrice;
	private Integer installmentNumber;

	public Transaction() {
	}

	public Transaction(Long id, String name, String description, LocalDate date, boolean installmentPurchase,
			Integer installmentCount, Double price, Double installmentPrice, Integer installmentNumber) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.date = date;
		this.installmentPurchase = installmentPurchase;
		this.installmentCount = installmentCount;
		this.price = price;
		this.installmentPrice = installmentPrice;
		this.installmentNumber = installmentNumber;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public CreditCardBill getCreditCardBill() {
		return creditCardBill;
	}

	public void setCreditCardBill(CreditCardBill creditCardBill) {
		this.creditCardBill = creditCardBill;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public boolean isInstallmentPurchase() {
		return installmentPurchase;
	}

	public void setInstallmentPurchase(boolean installmentPurchase) {
		this.installmentPurchase = installmentPurchase;
	}

	public Integer getInstallmentCount() {
		return installmentCount;
	}

	public void setInstallmentCount(Integer installmentCount) {
		this.installmentCount = installmentCount;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public Double getInstallmentPrice() {
		return installmentPrice;
	}

	public void setInstallmentPrice(Double installmentPrice) {
		this.installmentPrice = installmentPrice;
	}

	public Integer getInstallmentNumber() {
		return installmentNumber;
	}

	public void setInstallmentNumber(Integer installmentNumber) {
		this.installmentNumber = installmentNumber;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Transaction other = (Transaction) obj;
		return Objects.equals(id, other.id);
	}
}
