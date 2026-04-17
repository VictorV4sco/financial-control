package com.financial_control.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.financial_control.dtos.AccountsPayableReadDTO;
import com.financial_control.entities.AccountsPayable;

@Repository
public interface AccountsPayableRepository extends JpaRepository<AccountsPayable, Long> {

	@Query("""
			SELECT new com.financial_control.dtos.AccountsPayableReadDTO(
			    a.id,
			    a.description,
			    a.amount,
			    a.dueDate,
			    a.status
			)
			FROM AccountsPayable a
			WHERE FUNCTION('MONTH', a.dueDate) = :month
			AND FUNCTION('YEAR', a.dueDate) = :year
			""")
	List<AccountsPayableReadDTO> findByMonthAndYear(@Param("month") int month, @Param("year") int year);
}
