package com.banking.gateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
@RequestMapping("/generator")
@RequiredArgsConstructor
public class GeneratorProxyController {

	@Value("${gateway.generator-url:http://banking-generator:8090}")
	private String generatorUrl;

	private final RestTemplate restTemplate;

	@PostMapping("/start")
	public ResponseEntity<String> start(@RequestParam(defaultValue = "uniform") String scenario,
										@RequestParam(defaultValue = "10") int rps,
										@RequestParam(defaultValue = "60") int duration) {
		try {
			String url = generatorUrl + "/generator/start?scenario=" + scenario + "&rps=" + rps + "&duration=" + duration;
			return restTemplate.postForEntity(url, null, String.class);
		} catch (ResourceAccessException e) {
			log.warn("Generator unavailable: {}", e.getMessage());
			return ResponseEntity.status(503).body("{\"error\":\"Generator unavailable\"}");
		}
	}

	@PostMapping("/stop")
	public ResponseEntity<String> stop() {
		try {
			return restTemplate.postForEntity(generatorUrl + "/generator/stop", null, String.class);
		} catch (ResourceAccessException e) {
			log.warn("Generator unavailable: {}", e.getMessage());
			return ResponseEntity.status(503).body("{\"error\":\"Generator unavailable\"}");
		}
	}

	@PostMapping("/reset")
	public ResponseEntity<String> reset() {
		try {
			return restTemplate.postForEntity(generatorUrl + "/generator/reset", null, String.class);
		} catch (ResourceAccessException e) {
			log.warn("Generator unavailable: {}", e.getMessage());
			return ResponseEntity.status(503).body("{\"error\":\"Generator unavailable\"}");
		}
	}

	@GetMapping("/status")
	public ResponseEntity<String> status() {
		try {
			return restTemplate.getForEntity(generatorUrl + "/generator/status", String.class);
		} catch (ResourceAccessException e) {
			return ResponseEntity.status(503)
					.body("{\"running\":false,\"scenario\":\"unknown\",\"sent\":0,\"errors\":0,\"generatorOffline\":true}");
		}
	}
}
