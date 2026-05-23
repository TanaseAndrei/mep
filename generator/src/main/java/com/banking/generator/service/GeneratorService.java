package com.banking.generator.service;

import com.banking.generator.model.BankingRequestFactory;
import com.banking.generator.model.BankingRequestFactory.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class GeneratorService {

	private static final Logger log = LoggerFactory.getLogger(GeneratorService.class);

	@Value("${generator.gateway-url:http://gateway:8080}")
	private String gatewayUrl;

	private final RestTemplate restTemplate = new RestTemplate();
	private final AtomicLong sent = new AtomicLong(0);
	private final AtomicLong errors = new AtomicLong(0);

	private volatile boolean running = false;
	private volatile String scenario = "uniform";
	private ScheduledExecutorService scheduler;
	private ExecutorService httpPool;

	public void start(String scenarioName, int rps, int durationSec) {
		if (running) return;
		this.scenario = scenarioName;
		this.running = true;
		sent.set(0);
		errors.set(0);
		scheduler = Executors.newScheduledThreadPool(8);

		scheduler = Executors.newScheduledThreadPool(4);
		int poolSize = Math.max(50, (int)(rps * 0.35) + 20);
		httpPool = Executors.newFixedThreadPool(poolSize);

		log.info("Generator START — scenario={} rps={} duration={}s", scenarioName, rps, durationSec);

		switch (scenarioName) {
			case "burst" -> runBurst(rps);
			case "gradual" -> runGradual(rps);
			default -> runUniform(rps);
		}

		scheduler.schedule(this::stop, durationSec, TimeUnit.SECONDS);
	}

	public void stop() {
		running = false;
		if (scheduler != null) scheduler.shutdown();
		if (httpPool != null) httpPool.shutdown();
		log.info("Generator STOP — sent={} errors={}", sent.get(), errors.get());
	}

	private void runUniform(int rps) {
		long intervalMs = 1000L / rps;
		scheduler.scheduleAtFixedRate(
				() -> submitSend(BankingRequestFactory.uniform()),
				0, intervalMs, TimeUnit.MILLISECONDS
		);
	}

	private void runBurst(int rps) {
		int baseline = Math.max(1, rps / 4);
		scheduler.scheduleAtFixedRate(
				() -> submitSend(BankingRequestFactory.burst()),
				0, 1000L / baseline, TimeUnit.MILLISECONDS
		);
		scheduler.scheduleAtFixedRate(() -> {
			if (!running) return;
			log.info("BURST spike — {} req", rps * 5);
			for (int i = 0; i < rps * 5; i++)
				scheduler.submit(() -> submitSend(BankingRequestFactory.burst()));
		}, 15, 15, TimeUnit.SECONDS);
	}

	private void runGradual(int maxRps) {
		scheduler.submit(() -> gradualStep(1, maxRps, 0));
	}

	private void gradualStep(int currentRps, int maxRps, int phase) {
		if (!running || currentRps > maxRps) return;
		log.info("Gradual phase={} rps={}", phase, currentRps);
		int finalPhase = phase;
		ScheduledFuture<?> f = scheduler.scheduleAtFixedRate(
				() -> submitSend(BankingRequestFactory.gradual(finalPhase)),
				0, 1000L / currentRps, TimeUnit.MILLISECONDS
		);
		int nextRps = Math.min(currentRps + Math.max(1, maxRps / 10), maxRps);
		int nextPhase = Math.min(phase + 1, 9);
		scheduler.schedule(() -> {
			f.cancel(false);
			gradualStep(nextRps, maxRps, nextPhase);
		}, 10, TimeUnit.SECONDS);
	}

	private void submitSend(Request req) {
		if (!running) return;
		httpPool.submit(() -> send(req));
	}

	private void send(Request req) {
		if (!running) return;
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpMethod method = "GET".equals(req.method()) ? HttpMethod.GET : HttpMethod.POST;
			restTemplate.exchange(
					gatewayUrl + req.path(), method,
					new HttpEntity<>(req.body(), headers), String.class
			);
			long count = sent.incrementAndGet();
			if (count % 200 == 0) log.info("Sent {} (errors: {})", count, errors.get());
		} catch (Exception e) {
			errors.incrementAndGet();
			log.debug("Failed [{}]: {}", req.path(), e.getMessage());
		}
	}

	public boolean isRunning() {
		return running;
	}

	public long getSent() {
		return sent.get();
	}

	public long getErrors() {
		return errors.get();
	}

	public String getScenario() {
		return scenario;
	}
}
