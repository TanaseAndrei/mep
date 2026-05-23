package com.banking.gateway.config;

import com.banking.gateway.model.Instance;
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

	public static class GatewayProperties {
		private String strategy = "round-robin";
		private List<InstanceProperties> instances;

		public String getStrategy() {
			return strategy;
		}

		public void setStrategy(String s) {
			this.strategy = s;
		}

		public List<InstanceProperties> getInstances() {
			return instances;
		}

		public void setInstances(List<InstanceProperties> i) {
			this.instances = i;
		}
	}

	public static class InstanceProperties {
		private String id;
		private String url;
		private int weight = 1;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public int getWeight() {
			return weight;
		}

		public void setWeight(int w) {
			this.weight = w;
		}
	}
}
