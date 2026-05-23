package com.banking.instance.repository;

import com.banking.instance.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
	List<Transaction> findByFromIbanOrToIban(String from, String to);

	List<Transaction> findByStatus(Transaction.TransactionStatus status);
}
