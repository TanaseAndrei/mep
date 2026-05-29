package com.banking.gateway.service;

import com.banking.gateway.model.Instance;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProxyService {

	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final LoadBalancerService loadBalancer;
	private final MetricsService metricsService;

	public ResponseEntity<String> proxy(HttpServletRequest request, String body) {
		Instance instance = loadBalancer.select();
		String query = request.getQueryString();
		String targetUrl = instance.getUrl() + request.getRequestURI()
				+ (query != null ? "?" + query : "");

		long start = System.currentTimeMillis();

		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			Collections.list(request.getHeaderNames()).forEach(name -> {
				if (!name.equalsIgnoreCase("host") && !name.equalsIgnoreCase("content-length")) {
					headers.add(name, request.getHeader(name));
				}
			});
			headers.set("X-Gateway-Strategy", loadBalancer.getStrategy());
			headers.set("X-Routed-To", instance.getId());

			ResponseEntity<String> response = restTemplate.exchange(
					targetUrl,
					HttpMethod.valueOf(request.getMethod()),
					new HttpEntity<>(body, headers),
					String.class
			);

			long latency = System.currentTimeMillis() - start;
			instance.recordEnd(latency);
			metricsService.monitor(latency);

			log.debug("[{}] {} → {} ({}ms)", loadBalancer.getStrategy(),
					request.getRequestURI(), instance.getId(), latency);
			return response;

		} catch (Exception e) {
			long latency = System.currentTimeMillis() - start;
			instance.recordEnd(latency);
			instance.recordError();
			metricsService.monitor(latency);
			metricsService.recordError();
			log.warn("Proxy error → {}: {}", instance.getId(), e.getMessage());
			try {
				String errorBody = objectMapper.writeValueAsString(
						Map.of("error", "Instance unavailable", "instance", instance.getId()));
				return ResponseEntity.status(502).body(errorBody);
			} catch (Exception ex) {
				return ResponseEntity.status(502).body("{\"error\":\"Instance unavailable\"}");
			}
		}
	}
}
