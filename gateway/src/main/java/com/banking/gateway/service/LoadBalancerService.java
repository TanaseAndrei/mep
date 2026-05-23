package com.banking.gateway.service;

import com.banking.gateway.config.GatewayConfig.GatewayProperties;
import com.banking.gateway.model.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class LoadBalancerService {

	private static final Logger log = LoggerFactory.getLogger(LoadBalancerService.class);

	private final List<Instance> instances;
	private final GatewayProperties props;
	private final AtomicInteger rrIndex = new AtomicInteger(0);
	private List<Instance> weightedPool;

	public LoadBalancerService(List<Instance> instances, GatewayProperties props) {
		this.instances = instances;
		this.props = props;
		this.weightedPool = buildWeightedPool(instances);
		log.info("LoadBalancer ready — strategy={}, instances={}", props.getStrategy(), instances.size());
	}

	public Instance select() {
		return switch (props.getStrategy()) {
			case "least-connections" -> leastConnections();
			case "weighted" -> weighted();
			default -> roundRobin();
		};
	}

	private Instance roundRobin() {
		int idx = Math.abs(rrIndex.getAndIncrement() % instances.size());
		return instances.get(idx);
	}

	private Instance leastConnections() {
		return instances.stream()
				.min(Comparator.comparingInt(Instance::getActiveConnections))
				.orElse(instances.get(0));
	}

	private Instance weighted() {
		int idx = Math.abs(rrIndex.getAndIncrement() % weightedPool.size());
		return weightedPool.get(idx);
	}

	private List<Instance> buildWeightedPool(List<Instance> src) {
		List<Instance> pool = new ArrayList<>();
		for (Instance inst : src)
			for (int i = 0; i < inst.getWeight(); i++) pool.add(inst);
		return pool;
	}

	public void setStrategy(String strategy) {
		props.setStrategy(strategy);
		rrIndex.set(0);
		log.info("Strategy → {}", strategy);
	}

	public String getStrategy() {
		return props.getStrategy();
	}

	public List<Instance> getAllInstances() {
		return instances;
	}
}
