package com.financial_control.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.financial_control.dtos.TransactionReadDTO;
import com.financial_control.entities.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

	List<Transaction> findAllByCreditCardBillId(Long creditCardBillId);

	@Modifying
	@Query("DELETE FROM Transaction t WHERE t.creditCardBill.id = :creditCardBillId")
	void deleteByCreditCardBillId(@Param("creditCardBillId") Long creditCardBillId);

	@Query("""
			SELECT new com.financial_control.dtos.TransactionReadDTO(
			    t.id,
			    t.creditCardBill.id,
			    t.name,
			    t.description,
			    t.date,
			    t.installmentPurchase,
			    t.installmentCount,
			    t.price,
			    t.installmentPrice,
			    t.installmentNumber
			)
			FROM Transaction t
			WHERE t.creditCardBill.id = :creditCardBillId
			ORDER BY t.installmentNumber
			""")
	List<TransactionReadDTO> findByCreditCardBillId(@Param("creditCardBillId") Long creditCardBillId);
}
