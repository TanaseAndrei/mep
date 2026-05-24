package com.banking.generator.controller;

import com.banking.generator.service.GeneratorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/generator")
public class GeneratorController {

	private final GeneratorService generatorService;

	public GeneratorController(GeneratorService generatorService) {
		this.generatorService = generatorService;
	}

	@PostMapping("/start")
	public ResponseEntity<Map<String, Object>> start(
			@RequestParam(defaultValue = "uniform") String scenario,
			@RequestParam(defaultValue = "10") int rps,
			@RequestParam(defaultValue = "60") int duration) {
		if (generatorService.isRunning()) {
			return ResponseEntity.badRequest().body(Map.of("error", "Already running"));
		}
		generatorService.start(scenario, rps, duration);
		return ResponseEntity.ok(Map.of(
				"status", "started", "scenario", scenario,
				"rps", rps, "durationSeconds", duration
		));
	}

	@PostMapping("/stop")
	public ResponseEntity<Map<String, Object>> stop() {
		generatorService.stop();
		return ResponseEntity.ok(Map.of(
				"status", "stopped",
				"sent", generatorService.getSent(),
				"errors", generatorService.getErrors()
		));
	}

	@GetMapping("/status")
	public ResponseEntity<Map<String, Object>> status() {
		return ResponseEntity.ok(Map.of(
				"running", generatorService.isRunning(),
				"scenario", generatorService.getScenario(),
				"sent", generatorService.getSent(),
				"errors", generatorService.getErrors()
		));
	}
}
