package com.banking.gateway.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class MetricsService {

	private static final int MAX_SAMPLES = 50_000;

	private ConcurrentLinkedQueue<Long> latencies = new ConcurrentLinkedQueue<>();
	private final AtomicLong totalRequests = new AtomicLong(0);
	private final AtomicLong totalErrors   = new AtomicLong(0);
	private volatile long    startTime     = System.currentTimeMillis();

	public void monitor(long latencyMs) {
		totalRequests.incrementAndGet();
		latencies.offer(latencyMs);
		while (latencies.size() > MAX_SAMPLES) latencies.poll();
	}

	public void recordError() {
		totalErrors.incrementAndGet();
	}

	public double getMean() {
		List<Long> snap = new ArrayList<>(latencies);
		return snap.isEmpty() ? 0 : snap.stream().mapToLong(Long::longValue).average().orElse(0);
	}

	public double getP95() {
		return percentile(95);
	}

	public double getP99() {
		return percentile(99);
	}

	private double percentile(int pct) {
		List<Long> sorted = new ArrayList<>(latencies);
		if (sorted.isEmpty()) return 0;
		Collections.sort(sorted);
		int idx = (int) Math.ceil(pct / 100.0 * sorted.size()) - 1;
		return sorted.get(Math.max(0, idx));
	}

	public double getThroughput() {
		long elapsed = System.currentTimeMillis() - startTime;
		return elapsed == 0 ? 0 : totalRequests.get() * 1000.0 / elapsed;
	}

	public Map<String, Object> getSummary() {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("totalRequests", totalRequests.get());
		m.put("totalErrors", totalErrors.get());
		m.put("throughputRps", String.format("%.2f", getThroughput()));
		m.put("meanLatencyMs", String.format("%.2f", getMean()));
		m.put("p95LatencyMs", String.format("%.2f", getP95()));
		m.put("p99LatencyMs", String.format("%.2f", getP99()));
		return m;
	}

	public long getTotalRequests() {
		return totalRequests.get();
	}

	public long getTotalErrors() {
		return totalErrors.get();
	}

	public void reset() {
		latencies = new ConcurrentLinkedQueue<>();
		totalRequests.set(0);
		totalErrors.set(0);
		startTime = System.currentTimeMillis();
	}
}
