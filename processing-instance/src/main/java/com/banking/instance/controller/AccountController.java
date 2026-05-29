package com.banking.instance.controller;

import com.banking.instance.model.BankAccount;
import com.banking.instance.repository.AccountRepository;
import com.banking.instance.service.LatencySimulator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

	private final AccountRepository accounts;
	private final LatencySimulator simulator;

	@PostMapping
	public ResponseEntity<BankAccount> create(@RequestBody BankAccount account) {
		simulator.simulate();
		return ResponseEntity.status(HttpStatus.CREATED).body(accounts.save(account));
	}

	@GetMapping
	public ResponseEntity<List<BankAccount>> getAll() {
		simulator.simulate();
		return ResponseEntity.ok(accounts.findAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<BankAccount> getById(@PathVariable String id) {
		simulator.simulate();
		return accounts.findById(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/iban/{iban}")
	public ResponseEntity<BankAccount> getByIban(@PathVariable String iban) {
		simulator.simulate();
		return accounts.findByIban(iban)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@PostMapping("/{id}/deposit")
	public ResponseEntity<?> deposit(@PathVariable String id,
									 @RequestBody Map<String, BigDecimal> body) {
		simulator.simulate();
		return accounts.findById(id).map(acc -> {
			acc.setBalance(acc.getBalance().add(body.get("amount")));
			return ResponseEntity.ok(accounts.save(acc));
		}).orElse(ResponseEntity.notFound().build());
	}

	@PostMapping("/{id}/withdraw")
	public ResponseEntity<?> withdraw(@PathVariable String id,
									  @RequestBody Map<String, BigDecimal> body) {
		simulator.simulate();
		return accounts.findById(id).map(acc -> {
			BigDecimal amount = body.get("amount");
			if (acc.getBalance().compareTo(amount) < 0) {
				return ResponseEntity.badRequest().body(Map.of("error", "Insufficient funds"));
			}
			acc.setBalance(acc.getBalance().subtract(amount));
			return ResponseEntity.ok((Object) accounts.save(acc));
		}).orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/stats")
	public ResponseEntity<Map<String, Object>> stats() {
		simulator.simulate();
		return ResponseEntity.ok(Map.of(
				"totalAccounts", accounts.count(),
				"instanceId", simulator.getInstanceId()
		));
	}
}
