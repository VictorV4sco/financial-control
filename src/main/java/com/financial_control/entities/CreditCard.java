package com.financial_control.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class CreditCard {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String name;
	
	@OneToMany(mappedBy = "creditCard", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<CreditCardBill> bills = new ArrayList<>();
	
	public CreditCard() {
	}

	public CreditCard(Long id, String name) {
		super();
		this.id = id;
		this.name = name;
	}
	
	public void addBill(CreditCardBill bill) {
	    if (bill != null) {
	        bills.add(bill);
	        bill.setCreditCard(this);
	    }
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<CreditCardBill> getBill() {
		return bills;
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
		CreditCard other = (CreditCard) obj;
		return Objects.equals(id, other.id);
	}
	
	
}
