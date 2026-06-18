package com.ssv.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

	// Capacitor mobile WebView origins. Android (androidScheme: 'https') serves the
	// app from https://localhost; iOS serves it from capacitor://localhost. These
	// are always allowed so the mobile app works regardless of the per-environment
	// CORS_ALLOWED_ORIGINS value (which configures the browser/web origins).
	private static final List<String> CAPACITOR_ORIGINS = List.of("https://localhost", "capacitor://localhost");

	private final List<String> allowedOrigins;

	public CorsConfig(@Value("${app.cors.allowed-origins}") List<String> allowedOrigins) {
		List<String> merged = new ArrayList<>(allowedOrigins);
		for (String origin : CAPACITOR_ORIGINS) {
			if (!merged.contains(origin)) {
				merged.add(origin);
			}
		}
		this.allowedOrigins = List.copyOf(merged);
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(allowedOrigins);
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
