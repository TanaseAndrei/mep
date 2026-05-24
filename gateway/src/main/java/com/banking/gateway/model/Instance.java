package com.banking.gateway.model;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Instance {

	private final String id;
	private final String url;
	private final int weight;

	private final AtomicInteger activeConnections = new AtomicInteger(0);
	private final AtomicLong totalRequests = new AtomicLong(0);
	private final AtomicLong totalLatencyMs = new AtomicLong(0);
	private final AtomicLong totalErrors = new AtomicLong(0);
	private final AtomicLong idleTimeMs = new AtomicLong(0);
	private final AtomicLong lastRequestEnd = new AtomicLong(System.currentTimeMillis());

	public Instance(String id, String url, int weight) {
		this.id = id;
		this.url = url;
		this.weight = weight;
	}

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

	public String getId() {
		return id;
	}

	public String getUrl() {
		return url;
	}

	public int getWeight() {
		return weight;
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

	public long getTotalLatencyMs() {
		return totalLatencyMs.get();
	}

	public long getIdleTimeMs() {
		return idleTimeMs.get();
	}

	public void reset() {
		totalRequests.set(0);
		totalLatencyMs.set(0);
		totalErrors.set(0);
		idleTimeMs.set(0);
		lastRequestEnd.set(System.currentTimeMillis());
	}
}
