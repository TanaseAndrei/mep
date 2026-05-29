package com.banking.instance.controller;

import com.banking.instance.service.LatencySimulator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class BankingController {

	private final LatencySimulator simulator;

	@GetMapping("/health")
	public ResponseEntity<Map<String, Object>> health() {
		return ResponseEntity.ok(Map.of(
				"status", "UP",
				"instanceId", simulator.getInstanceId(),
				"baseProcessingMs", simulator.getBaseProcessingMs(),
				"processed", simulator.getProcessedCount()
		));
	}
}
