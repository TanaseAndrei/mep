package com.banking.instance.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class LatencySimulator {

	private static final Logger log = LoggerFactory.getLogger(LatencySimulator.class);
	private static final Random random = new Random();

	@Value("${instance.id}")
	private String instanceId;

	@Value("${instance.base-processing-ms}")
	private int baseProcessingMs;

	@Value("${instance.jitter-ms}")
	private int jitterMs;

	private final AtomicLong processedCount = new AtomicLong(0);

	public void simulate() {
		int sleepMs = baseProcessingMs + (jitterMs > 0 ? random.nextInt(jitterMs) : 0);
		try {
			Thread.sleep(sleepMs);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		processedCount.incrementAndGet();
		log.debug("[{}] processed request in ~{}ms", instanceId, sleepMs);
	}

	public String getInstanceId() {
		return instanceId;
	}

	public int getBaseProcessingMs() {
		return baseProcessingMs;
	}

	public long getProcessedCount() {
		return processedCount.get();
	}
}
