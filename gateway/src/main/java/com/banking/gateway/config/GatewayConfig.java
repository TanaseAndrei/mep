package com.banking.gateway.config;

import com.banking.gateway.model.Instance;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableConfigurationProperties
public class GatewayConfig {

	@Bean
	@ConfigurationProperties(prefix = "gateway")
	public GatewayProperties gatewayProperties() {
		return new GatewayProperties();
	}

	@Bean
	public List<Instance> instances(GatewayProperties props) {
		return props.getInstances().stream()
				.map(p -> new Instance(p.getId(), p.getUrl(), p.getWeight()))
				.toList();
	}

	@Getter
	@Setter
	public static class GatewayProperties {
		private String strategy;
		private List<InstanceProperties> instances;
	}

	@Getter
	@Setter
	public static class InstanceProperties {
		private String id;
		private String url;
		private int weight;
	}
}
