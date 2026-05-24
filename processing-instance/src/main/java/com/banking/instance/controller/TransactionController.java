package com.banking.instance.controller;

import com.banking.instance.model.Transaction;
import com.banking.instance.repository.TransactionRepository;
import com.banking.instance.service.LatencySimulator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

	private final TransactionRepository transactions;
	private final LatencySimulator simulator;

	public TransactionController(TransactionRepository transactions, LatencySimulator simulator) {
		this.transactions = transactions;
		this.simulator = simulator;
	}

	@PostMapping
	public ResponseEntity<Transaction> create(@RequestBody Transaction txn) {
		simulator.simulate();
		txn.setStatus(Transaction.TransactionStatus.PENDING);
		Transaction saved = transactions.save(txn);
		saved.setStatus(Transaction.TransactionStatus.COMPLETED);
		return ResponseEntity.status(HttpStatus.CREATED).body(transactions.save(saved));
	}

	@GetMapping
	public ResponseEntity<List<Transaction>> getAll() {
		simulator.simulate();
		return ResponseEntity.ok(transactions.findAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Transaction> getById(@PathVariable String id) {
		simulator.simulate();
		return transactions.findById(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/iban/{iban}")
	public ResponseEntity<List<Transaction>> getByIban(@PathVariable String iban) {
		simulator.simulate();
		return ResponseEntity.ok(transactions.findByFromIbanOrToIban(iban, iban));
	}

	@GetMapping("/pending")
	public ResponseEntity<List<Transaction>> getPending() {
		simulator.simulate();
		return ResponseEntity.ok(transactions.findByStatus(Transaction.TransactionStatus.PENDING));
	}

	@GetMapping("/stats")
	public ResponseEntity<Map<String, Object>> stats() {
		simulator.simulate();
		return ResponseEntity.ok(Map.of(
				"totalTransactions", transactions.count(),
				"instanceId", simulator.getInstanceId()
		));
	}
}
