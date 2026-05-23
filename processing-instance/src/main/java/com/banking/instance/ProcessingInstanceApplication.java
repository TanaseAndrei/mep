package com.banking.instance;

import com.banking.instance.model.BankAccount;
import com.banking.instance.model.Payment;
import com.banking.instance.model.Transaction;
import com.banking.instance.repository.AccountRepository;
import com.banking.instance.repository.TransactionRepository;
import com.banking.instance.repository.PaymentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;

@SpringBootApplication
public class ProcessingInstanceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProcessingInstanceApplication.class, args);
	}

	@Bean
	CommandLineRunner seedData(AccountRepository accounts,
							   TransactionRepository transactions,
							   PaymentRepository payments) {
		return args -> {
			// Conturi seed
			accounts.save(new BankAccount("RO49AAAA1B31007593840000", "Ion Popescu",
					new BigDecimal("15000.00"), BankAccount.AccountType.CURRENT));
			accounts.save(new BankAccount("RO49AAAA1B31007593840001", "Maria Ionescu",
					new BigDecimal("42500.50"), BankAccount.AccountType.SAVINGS));
			accounts.save(new BankAccount("RO49AAAA1B31007593840002", "SC Alfa SRL",
					new BigDecimal("250000.00"), BankAccount.AccountType.BUSINESS));
			accounts.save(new BankAccount("RO49AAAA1B31007593840003", "Andrei Dumitrescu",
					new BigDecimal("3200.75"), BankAccount.AccountType.CURRENT));
			accounts.save(new BankAccount("RO49AAAA1B31007593840004", "Elena Constantin",
					new BigDecimal("88000.00"), BankAccount.AccountType.SAVINGS));

			// Tranzacții seed
			transactions.save(new Transaction("RO49AAAA1B31007593840002",
					"RO49AAAA1B31007593840000", new BigDecimal("12500.00"), "Salariu"));
			transactions.save(new Transaction("RO49AAAA1B31007593840000",
					"RO49AAAA1B31007593840001", new BigDecimal("500.00"), "Chirie"));

			// Plăți seed
			payments.save(new Payment("RO49AAAA1B31007593840000",
					"RO49AAAA1B31007593840001", new BigDecimal("1200.00"),
					Payment.PaymentMethod.SEPA_CREDIT, "Factura energie"));
		};
	}
}
