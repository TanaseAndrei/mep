package com.banking.instance.controller;

import com.banking.instance.model.BankAccount;
import com.banking.instance.model.Payment;
import com.banking.instance.model.Transaction;
import com.banking.instance.repository.AccountRepository;
import com.banking.instance.repository.PaymentRepository;
import com.banking.instance.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Controller unic pentru toate operațiile bancare.
 * Toate cele 3 instanțe rulează ACELAȘI cod — singura diferență
 * este latența simulată (BASE_PROCESSING_MS + jitter), configurată
 * prin variabile de mediu în docker-compose.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class BankingController {

	private static final Logger log = LoggerFactory.getLogger(BankingController.class);
	private static final Random random = new Random();

	@Value("${instance.id:instance-1}")
	private String instanceId;

	@Value("${instance.base-processing-ms:100}")
	private int baseProcessingMs;

	@Value("${instance.jitter-ms:30}")
	private int jitterMs;

	private final AccountRepository accounts;
	private final TransactionRepository transactions;
	private final PaymentRepository payments;
	private final AtomicLong processedCount = new AtomicLong(0);

	public BankingController(AccountRepository accounts,
							 TransactionRepository transactions,
							 PaymentRepository payments) {
		this.accounts = accounts;
		this.transactions = transactions;
		this.payments = payments;
	}

	// ── ACCOUNTS ──────────────────────────────────────────────────────────

	@PostMapping("/accounts")
	public ResponseEntity<BankAccount> createAccount(@RequestBody BankAccount account) {
		simulate();
		return ResponseEntity.status(HttpStatus.CREATED).body(accounts.save(account));
	}

	@GetMapping("/accounts")
	public ResponseEntity<List<BankAccount>> getAccounts() {
		simulate();
		return ResponseEntity.ok(accounts.findAll());
	}

	@GetMapping("/accounts/{id}")
	public ResponseEntity<BankAccount> getAccount(@PathVariable String id) {
		simulate();
		return accounts.findById(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/accounts/iban/{iban}")
	public ResponseEntity<BankAccount> getAccountByIban(@PathVariable String iban) {
		simulate();
		return accounts.findByIban(iban)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@PostMapping("/accounts/{id}/deposit")
	public ResponseEntity<?> deposit(@PathVariable String id,
									 @RequestBody Map<String, BigDecimal> body) {
		simulate();
		return accounts.findById(id).map(acc -> {
			acc.setBalance(acc.getBalance().add(body.get("amount")));
			return ResponseEntity.ok(accounts.save(acc));
		}).orElse(ResponseEntity.notFound().build());
	}

	@PostMapping("/accounts/{id}/withdraw")
	public ResponseEntity<?> withdraw(@PathVariable String id,
									  @RequestBody Map<String, BigDecimal> body) {
		simulate();
		return accounts.findById(id).map(acc -> {
			BigDecimal amount = body.get("amount");
			if (acc.getBalance().compareTo(amount) < 0)
				return ResponseEntity.badRequest().body(Map.of("error", "Insufficient funds"));
			acc.setBalance(acc.getBalance().subtract(amount));
			return ResponseEntity.ok((Object) accounts.save(acc));
		}).orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/accounts/stats")
	public ResponseEntity<Map<String, Object>> accountStats() {
		simulate();
		return ResponseEntity.ok(Map.of(
				"totalAccounts", accounts.count(),
				"instanceId", instanceId
		));
	}

	// ── TRANSACTIONS ──────────────────────────────────────────────────────

	@PostMapping("/transactions")
	public ResponseEntity<Transaction> createTransaction(@RequestBody Transaction txn) {
		simulate();
		txn.setStatus(Transaction.TransactionStatus.PENDING);
		Transaction saved = transactions.save(txn);
		// Procesare imediată
		saved.setStatus(Transaction.TransactionStatus.COMPLETED);
		return ResponseEntity.status(HttpStatus.CREATED).body(transactions.save(saved));
	}

	@GetMapping("/transactions")
	public ResponseEntity<List<Transaction>> getTransactions() {
		simulate();
		return ResponseEntity.ok(transactions.findAll());
	}

	@GetMapping("/transactions/{id}")
	public ResponseEntity<Transaction> getTransaction(@PathVariable String id) {
		simulate();
		return transactions.findById(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/transactions/iban/{iban}")
	public ResponseEntity<List<Transaction>> getTransactionsByIban(@PathVariable String iban) {
		simulate();
		return ResponseEntity.ok(transactions.findByFromIbanOrToIban(iban, iban));
	}

	@GetMapping("/transactions/pending")
	public ResponseEntity<List<Transaction>> getPending() {
		simulate();
		return ResponseEntity.ok(transactions.findByStatus(Transaction.TransactionStatus.PENDING));
	}

	@GetMapping("/transactions/stats")
	public ResponseEntity<Map<String, Object>> txnStats() {
		simulate();
		return ResponseEntity.ok(Map.of(
				"totalTransactions", transactions.count(),
				"instanceId", instanceId
		));
	}

	// ── PAYMENTS ──────────────────────────────────────────────────────────

	@PostMapping("/payments")
	public ResponseEntity<Payment> createPayment(@RequestBody Payment payment) {
		simulate();
		// Fraud detection: SEPA_INSTANT > 50.000 RON → REJECTED
		if (payment.getMethod() == Payment.PaymentMethod.SEPA_INSTANT
				&& payment.getAmount().compareTo(new BigDecimal("50000")) > 0) {
			payment.setStatus(Payment.PaymentStatus.REJECTED);
		} else {
			payment.setStatus(Payment.PaymentStatus.EXECUTED);
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(payments.save(payment));
	}

	@GetMapping("/payments")
	public ResponseEntity<List<Payment>> getPayments() {
		simulate();
		return ResponseEntity.ok(payments.findAll());
	}

	@GetMapping("/payments/{id}")
	public ResponseEntity<Payment> getPayment(@PathVariable String id) {
		simulate();
		return payments.findById(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/payments/debtor/{iban}")
	public ResponseEntity<List<Payment>> getByDebtor(@PathVariable String iban) {
		simulate();
		return ResponseEntity.ok(payments.findByDebtorIban(iban));
	}

	@GetMapping("/payments/stats")
	public ResponseEntity<Map<String, Object>> paymentStats() {
		simulate();
		return ResponseEntity.ok(Map.of(
				"totalPayments", payments.count(),
				"instanceId", instanceId
		));
	}

	// ── Health ────────────────────────────────────────────────────────────

	@GetMapping("/health")
	public ResponseEntity<Map<String, Object>> health() {
		return ResponseEntity.ok(Map.of(
				"status", "UP",
				"instanceId", instanceId,
				"baseProcessingMs", baseProcessingMs,
				"processed", processedCount.get()
		));
	}

	// ── Simulare latență ──────────────────────────────────────────────────

	/**
	 * Simulează timpul de procesare al instanței.
	 * BASE_PROCESSING_MS diferă per instanță (100/150/200ms) — aceasta
	 * este singura diferență între cele 3 noduri, justificând weighted scheduling.
	 */
	private void simulate() {
		int sleepMs = baseProcessingMs + (jitterMs > 0 ? random.nextInt(jitterMs) : 0);
		try {
			Thread.sleep(sleepMs);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		processedCount.incrementAndGet();
		log.debug("[{}] processed request in ~{}ms", instanceId, sleepMs);
	}
}
