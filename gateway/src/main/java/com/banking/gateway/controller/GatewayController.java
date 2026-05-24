package com.banking.gateway.controller;

import com.banking.gateway.model.Instance;
import com.banking.gateway.service.LoadBalancerService;
import com.banking.gateway.service.MetricsService;
import com.banking.gateway.service.ProxyService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class GatewayController {

	private final ProxyService proxyService;
	private final LoadBalancerService loadBalancer;
	private final MetricsService metricsService;

	public GatewayController(ProxyService proxyService,
							 LoadBalancerService loadBalancer,
							 MetricsService metricsService) {
		this.proxyService = proxyService;
		this.loadBalancer = loadBalancer;
		this.metricsService = metricsService;
	}

	@RequestMapping("/api/**")
	public ResponseEntity<String> proxy(HttpServletRequest request,
										@RequestBody(required = false) String body) {
		return proxyService.proxy(request, body);
	}

	@GetMapping("/gateway/metrics")
	public ResponseEntity<Map<String, Object>> metrics() {
		long totalReqs = loadBalancer.getAllInstances().stream()
				.mapToLong(Instance::getTotalRequests).sum();

		List<Map<String, Object>> instances = loadBalancer.getAllInstances().stream()
				.map(inst -> {
					Map<String, Object> m = new LinkedHashMap<>();
					m.put("id", inst.getId());
					m.put("activeConnections", inst.getActiveConnections());
					m.put("totalRequests", inst.getTotalRequests());
					m.put("totalErrors", inst.getTotalErrors());
					m.put("avgLatencyMs", String.format("%.2f", inst.getAvgLatencyMs()));
					m.put("idleTimeMs", inst.getIdleTimeMs());
					double util = totalReqs == 0 ? 0 : (double) inst.getTotalRequests() / totalReqs * 100;
					m.put("utilizationPct", String.format("%.1f", util));
					return m;
				})
				.toList();

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("strategy", loadBalancer.getStrategy());
		result.put("gateway", metricsService.getSummary());
		result.put("instances", instances);
		return ResponseEntity.ok(result);
	}

	@PostMapping("/gateway/metrics/reset")
	public ResponseEntity<Map<String, String>> resetMetrics() {
		metricsService.reset();
		loadBalancer.getAllInstances().forEach(Instance::reset);
		return ResponseEntity.ok(Map.of(
				"status", "reset",
				"strategy", loadBalancer.getStrategy(),
				"message", "All metrics cleared. Ready for next experiment."
		));
	}

	@PostMapping("/gateway/strategy")
	public ResponseEntity<Map<String, String>> changeStrategy(@RequestParam String name) {
		List<String> valid = List.of("round-robin", "least-connections", "weighted");
		if (!valid.contains(name)) {
			return ResponseEntity.badRequest().body(Map.of("error", "Valid: " + valid));
		}
		loadBalancer.setStrategy(name);
		return ResponseEntity.ok(Map.of("strategy", name, "status", "changed"));
	}

	@GetMapping("/gateway/health")
	public ResponseEntity<Map<String, String>> health() {
		return ResponseEntity.ok(Map.of("status", "UP", "strategy", loadBalancer.getStrategy()));
	}
}
