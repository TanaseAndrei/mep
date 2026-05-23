package com.banking.instance.repository;

import com.banking.instance.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<BankAccount, String> {
	Optional<BankAccount> findByIban(String iban);
}
