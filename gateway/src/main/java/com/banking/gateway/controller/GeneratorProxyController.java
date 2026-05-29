package com.banking.gateway.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

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
		String url = generatorUrl + "/generator/start?scenario=" + scenario + "&rps=" + rps + "&duration=" + duration;
		return restTemplate.postForEntity(url, null, String.class);
	}

	@PostMapping("/stop")
	public ResponseEntity<String> stop() {
		return restTemplate.postForEntity(generatorUrl + "/generator/stop", null, String.class);
	}

	@PostMapping("/reset")
	public ResponseEntity<String> reset() {
		return restTemplate.postForEntity(generatorUrl + "/generator/reset", null, String.class);
	}

	@GetMapping("/status")
	public ResponseEntity<String> status() {
		return restTemplate.getForEntity(generatorUrl + "/generator/status", String.class);
	}
}
