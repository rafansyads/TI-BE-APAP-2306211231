package apap.ti._5.accommodation_2306211231_be.security;

import apap.ti._5.accommodation_2306211231_be.security.jwt.JwtTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

	@Autowired
	@Lazy
	private UserDetailsService userDetailsService; // provided by ProfileUserDetailsService

	@Autowired
	private JwtTokenFilter jwtTokenFilter;

	// ===================== JWT API SECURITY =====================
	// RBAC not yet defined: all endpoints currently permitted.
	@Bean
	@Order(1)
	public SecurityFilterChain jwtFilterChain(HttpSecurity http) throws Exception {
		http.securityMatcher("/api/**")
				.cors(Customizer.withDefaults())
				.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(requests -> requests

						// always allow preflight requests
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

						.requestMatchers(HttpMethod.POST, "/auth/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/auth/**").permitAll()

						// Property endpoints RBAC
						.requestMatchers(HttpMethod.GET, "/api/property/**").hasAnyAuthority("SUPERADMIN", "ACCOMMODATION_OWNER", "CUSTOMER")
						.requestMatchers(HttpMethod.POST, "/api/property/**").hasAnyAuthority("SUPERADMIN", "ACCOMMODATION_OWNER")
						.requestMatchers(HttpMethod.PUT, "/api/property/**").hasAnyAuthority("SUPERADMIN", "ACCOMMODATION_OWNER")
						.requestMatchers(HttpMethod.DELETE, "/api/property/**").hasAnyAuthority("SUPERADMIN", "ACCOMMODATION_OWNER")

						// RoomType endpoints RBAC
						.requestMatchers(HttpMethod.GET, "/api/roomtype/**").hasAnyAuthority("SUPERADMIN", "ACCOMMODATION_OWNER", "CUSTOMER")
						.requestMatchers(HttpMethod.POST, "/api/roomtype/**").hasAnyAuthority("SUPERADMIN", "ACCOMMODATION_OWNER")
						.requestMatchers(HttpMethod.PUT, "/api/roomtype/**").hasAnyAuthority("SUPERADMIN", "ACCOMMODATION_OWNER")
						.requestMatchers(HttpMethod.DELETE, "/api/roomtype/**").hasAnyAuthority("SUPERADMIN", "ACCOMMODATION_OWNER")

						// Room endpoints RBAC
						.requestMatchers(HttpMethod.GET, "/api/room/**").hasAnyAuthority("SUPERADMIN", "ACCOMMODATION_OWNER", "CUSTOMER")
						.requestMatchers(HttpMethod.POST, "/api/room/**").hasAnyAuthority("SUPERADMIN", "ACCOMMODATION_OWNER")
						.requestMatchers(HttpMethod.PUT, "/api/room/**").hasAnyAuthority("SUPERADMIN", "ACCOMMODATION_OWNER")
						.requestMatchers(HttpMethod.DELETE, "/api/room/**").hasAnyAuthority("SUPERADMIN", "ACCOMMODATION_OWNER")

						// Booking endpoints RBAC
						.requestMatchers(HttpMethod.GET, "/api/bookings/**").hasAnyAuthority("SUPERADMIN", "ACCOMMODATION_OWNER", "CUSTOMER")
						.requestMatchers(HttpMethod.POST, "/api/bookings/create/**").hasAuthority("CUSTOMER")
						.requestMatchers(HttpMethod.POST, "/api/bookings/create").hasAuthority("CUSTOMER")
						.requestMatchers(HttpMethod.PUT, "/api/bookings/update").hasAuthority("CUSTOMER")
						.requestMatchers(HttpMethod.POST, "/api/bookings/status/pay").hasAuthority("API_KEY")
						.requestMatchers(HttpMethod.POST, "/api/bookings/status/cancel").hasAuthority("CUSTOMER")
						.requestMatchers(HttpMethod.POST, "/api/bookings/status/refund").hasAuthority("CUSTOMER")

						// Booking review endpoints RBAC
						.requestMatchers(HttpMethod.GET, "/api/bookings/reviews/**").hasAnyAuthority("SUPERADMIN", "ACCOMMODATION_OWNER", "CUSTOMER")
						.requestMatchers(HttpMethod.GET, "/api/bookings/reviews").hasAnyAuthority("SUPERADMIN", "ACCOMMODATION_OWNER", "CUSTOMER")
						.requestMatchers(HttpMethod.POST, "/api/bookings/reviews/create").hasAuthority("CUSTOMER")
						
						.anyRequest().authenticated())
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
				.exceptionHandling(e -> e
						.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
						.accessDeniedHandler(new AccessDeniedHandler() {
							@Override
							public void handle(HttpServletRequest request, HttpServletResponse response,
									org.springframework.security.access.AccessDeniedException accessDeniedException)
									throws IOException, ServletException {
								response.setStatus(HttpServletResponse.SC_FORBIDDEN);
								response.getWriter().write("Forbidden");
							}
						}));

		return http.build();
	}

	// ===================== WEB SECURITY (e.g. form login, static resources) =====================
	@Bean
	@Order(2)
	public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
		http.securityMatcher(request -> !request.getRequestURI().startsWith("/api"))
				.csrf(Customizer.withDefaults())
				.authorizeHttpRequests(requests -> requests
						.requestMatchers("/css/**", "/js/**", "/login").permitAll()
						.anyRequest().permitAll() // temporary: allow all until RBAC rules defined
				// change to .authenticated() to require login
				)
				.formLogin(form -> form
						.loginPage("/login")
						.permitAll()
						.defaultSuccessUrl("/"))
				.logout(logout -> logout
						.logoutUrl("/logout")
						.logoutSuccessUrl("/login"))
				.exceptionHandling(handling -> handling
						.accessDeniedHandler((request, response, accessDeniedException) -> {
							response.sendRedirect("/access-denied");
						}));

		return http.build();
	}

	// ===================== AUTH MANAGER & PASSWORD ENCODER =====================
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	// Removed in-memory users; now using persistent profile tables.

	@Autowired
	public void configAuthentication(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
	}
}
