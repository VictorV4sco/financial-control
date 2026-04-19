package com.financial_control.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.financial_control.dtos.CreditCardBillReadDTO;
import com.financial_control.entities.CreditCardBill;

@Repository
public interface CreditCardBillRepository extends JpaRepository<CreditCardBill, Long> {

	@Query("""
			SELECT new com.financial_control.dtos.CreditCardBillReadDTO(
			    b.id,
			    b.creditCard.id,
			    b.openingDate,
			    b.closingDate,
			    b.dueDate,
			    b.totalAmount,
			    b.status
			)
			FROM CreditCardBill b
			WHERE b.creditCard.id = :creditCardId
			AND FUNCTION('YEAR', b.closingDate) = :year
			AND FUNCTION('MONTH', b.closingDate) = :month
			""")
	List<CreditCardBillReadDTO> findByCreditCardAndMonthAndYear(
			@Param("creditCardId") Long creditCardId,
			@Param("year") Integer year,
			@Param("month") Integer month);
}
