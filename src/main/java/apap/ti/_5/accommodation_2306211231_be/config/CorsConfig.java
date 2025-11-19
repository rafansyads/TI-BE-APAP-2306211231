package apap.ti._5.accommodation_2306211231_be.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class CorsConfig {

	private static final String DEV_FRONTEND_ORIGIN = "http://localhost:5173"; // Vue dev server

	@Value("${CORS_ALLOWED_ORIGINS}")
	private String allowedOrigins;

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(@NonNull CorsRegistry registry) {
				registry.addMapping("/**")
						.allowedOrigins(
              allowedOrigins != null && !allowedOrigins.isEmpty() ? allowedOrigins.split(",") : new String[] { DEV_FRONTEND_ORIGIN }
            )
						.allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
						.allowedHeaders("*")
						.exposedHeaders("Authorization")
						.allowCredentials(true)
						.maxAge(3600); // cache pre-flight for 1 hour
			}
		};
	}
}
