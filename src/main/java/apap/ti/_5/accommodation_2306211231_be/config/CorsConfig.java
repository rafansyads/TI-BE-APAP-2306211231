package apap.ti._5.accommodation_2306211231_be.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

	private static final String DEV_FRONTEND_ORIGIN = "http://localhost:5173"; // Vue dev server

	// Provide a safe default for local/dev if env var is absent.
	// In production, set CORS_ALLOWED_ORIGINS to a comma-separated list, e.g.
	// https://app.example.com,https://admin.example.com
	@Value("${CORS_ALLOWED_ORIGINS:http://localhost:5173}")
	private String allowedOrigins;

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				String[] origins = allowedOrigins.trim().isEmpty() ? new String[] { DEV_FRONTEND_ORIGIN }
					: java.util.Arrays.stream(allowedOrigins.split(","))
						.map(String::trim)
						.filter(s -> !s.isEmpty())
						.toArray(String[]::new);

				registry.addMapping("/**")
					.allowedOrigins(origins)
					.allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
					.allowedHeaders("*")
					.exposedHeaders("Authorization", "X-Forward-Token")
					.allowCredentials(true)
					.maxAge(3600); // cache pre-flight for 1 hour
			}
		};
	}
}
