package com.financial_control.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.financial_control.dtos.CreditCardReadDTO;
import com.financial_control.entities.CreditCard;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {

	@Query("""
			SELECT new com.financial_control.dtos.CreditCardReadDTO(
			    c.id,
			    c.name
			)
			FROM CreditCard c
			""")
	List<CreditCardReadDTO> findAllCreditCard();
}
