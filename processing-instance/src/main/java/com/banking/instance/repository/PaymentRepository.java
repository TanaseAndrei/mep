package com.banking.instance.repository;

import com.banking.instance.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, String> {
	List<Payment> findByDebtorIban(String iban);
}
