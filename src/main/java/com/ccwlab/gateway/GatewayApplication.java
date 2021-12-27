package com.ccwlab.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class GatewayApplication {
	Logger logger = LoggerFactory.getLogger(GatewayApplication.class);

	@Autowired
	AuthenticationFilter authFilter;

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

	@LoadBalanced
	@Bean
	public RestTemplate loadbalancedRestTemplate() {
		return new RestTemplate();
	}

	@Autowired
	private DiscoveryClient discoveryClient;

	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
		logger.debug(discoveryClient.getInstances("main").get(0).getUri().toString());

		return builder.routes()
				.route("login",r -> r.path("/auth/login").and().method(HttpMethod.POST)
						.filters(f -> f.addResponseHeader("Access-Control-Expose-Headers", "Authorization"))
						.uri(discoveryClient.getInstances("main").get(0).getUri()))
				.route("public",r -> r.path("/works/*/logs").and().method(HttpMethod.GET)
						.filters(f ->
								f.rewritePath("/logs$", ""))
						.uri(discoveryClient.getInstances("controller").get(0).getUri()))
				.route("secured",r -> r.path("/**")
						.filters(f -> f.filter(authFilter))
						.uri(discoveryClient.getInstances("main").get(0).getUri()))
//				.route("secured",r -> r.path("/works/*/logs")
//						.filters(f ->
//								f.filter(authFilter).rewritePath("/logs$", ""))
//						.uri(discoveryClient.getInstances("controller").get(0).getUri()))

//				.route("host_route", r -> r.host("*.myhost.org")
//						.uri("http://httpbin.org"))
//				.route("rewrite_route", r -> r.host("*.rewrite.org")
//						.filters(f -> f.rewritePath("/foo/(?<segment>.*)", "/${segment}"))
//						.uri("http://httpbin.org"))
//				.route("hystrix_route", r -> r.host("*.hystrix.org")
//						.filters(f -> f.hystrix(c -> c.setName("slowcmd")))
//						.uri("http://httpbin.org"))
//				.route("hystrix_fallback_route", r -> r.host("*.hystrixfallback.org")
//						.filters(f -> f.hystrix(c -> c.setName("slowcmd").setFallbackUri("forward:/hystrixfallback")))
//						.uri("http://httpbin.org"))
//				.route("limit_route", r -> r
//						.host("*.limited.org").and().path("/anything/**")
//						.filters(f -> f.requestRateLimiter(c -> c.setRateLimiter(redisRateLimiter())))
//						.uri("http://httpbin.org"))
				.build();
	}
}
