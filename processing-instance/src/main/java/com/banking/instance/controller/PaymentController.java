package com.banking.instance.controller;

import com.banking.instance.model.Payment;
import com.banking.instance.repository.PaymentRepository;
import com.banking.instance.service.LatencySimulator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payments")
public class PaymentController {

	private final PaymentRepository payments;
	private final LatencySimulator simulator;

	public PaymentController(PaymentRepository payments, LatencySimulator simulator) {
		this.payments = payments;
		this.simulator = simulator;
	}

	@PostMapping
	public ResponseEntity<Payment> create(@RequestBody Payment payment) {
		simulator.simulate();
		if (payment.getMethod() == Payment.PaymentMethod.SEPA_INSTANT
				&& payment.getAmount().compareTo(new BigDecimal("50000")) > 0) {
			payment.setStatus(Payment.PaymentStatus.REJECTED);
		} else {
			payment.setStatus(Payment.PaymentStatus.EXECUTED);
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(payments.save(payment));
	}

	@GetMapping
	public ResponseEntity<List<Payment>> getAll() {
		simulator.simulate();
		return ResponseEntity.ok(payments.findAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Payment> getById(@PathVariable String id) {
		simulator.simulate();
		return payments.findById(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/debtor/{iban}")
	public ResponseEntity<List<Payment>> getByDebtor(@PathVariable String iban) {
		simulator.simulate();
		return ResponseEntity.ok(payments.findByDebtorIban(iban));
	}

	@GetMapping("/stats")
	public ResponseEntity<Map<String, Object>> stats() {
		simulator.simulate();
		return ResponseEntity.ok(Map.of(
				"totalPayments", payments.count(),
				"instanceId", simulator.getInstanceId()
		));
	}
}
