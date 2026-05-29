package com.banking.gateway.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@RequiredArgsConstructor
public class Instance {

	@Getter
	private final String id;
	@Getter
	private final String url;
	@Getter
	private final int weight;

	private final AtomicInteger activeConnections = new AtomicInteger(0);
	private final AtomicLong totalRequests = new AtomicLong(0);
	private final AtomicLong totalLatencyMs = new AtomicLong(0);
	private final AtomicLong totalErrors = new AtomicLong(0);
	private final AtomicLong idleTimeMs = new AtomicLong(0);
	private final AtomicLong lastRequestEnd = new AtomicLong(System.currentTimeMillis());

	public void recordStart() {
		long now = System.currentTimeMillis();
		long prevEnd = lastRequestEnd.getAndSet(now);
		if (activeConnections.get() == 0) {
			long idle = now - prevEnd;
			if (idle > 0) {
				idleTimeMs.addAndGet(idle);
			}
		}
		activeConnections.incrementAndGet();
		totalRequests.incrementAndGet();
	}

	public void recordEnd(long latencyMs) {
		activeConnections.decrementAndGet();
		totalLatencyMs.addAndGet(latencyMs);
		lastRequestEnd.set(System.currentTimeMillis());
	}

	public void recordError() {
		totalErrors.incrementAndGet();
	}

	public double getAvgLatencyMs() {
		long r = totalRequests.get();
		return r == 0 ? 0.0 : (double) totalLatencyMs.get() / r;
	}

	public int getActiveConnections() {
		return activeConnections.get();
	}

	public long getTotalRequests() {
		return totalRequests.get();
	}

	public long getTotalErrors() {
		return totalErrors.get();
	}

	public long getIdleTimeMs() {
		return idleTimeMs.get();
	}

	public void reset() {
		activeConnections.set(0);
		totalRequests.set(0);
		totalLatencyMs.set(0);
		totalErrors.set(0);
		idleTimeMs.set(0);
		lastRequestEnd.set(System.currentTimeMillis());
	}
}
