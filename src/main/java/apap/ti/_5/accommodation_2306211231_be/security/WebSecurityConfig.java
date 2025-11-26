package apap.ti._5.accommodation_2306211231_be.security;

import java.io.IOException;

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

import apap.ti._5.accommodation_2306211231_be.security.jwt.JwtTokenFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
		// Apply JWT security to all API endpoints (use /**). Previously this matched
		// only
		// "/api/**" which left controllers mapped at top-level (e.g. "/property")
		// unprotected.
		http.securityMatcher("/**")
				.cors(Customizer.withDefaults())
				.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(requests -> requests

						// always allow preflight requests
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

						.requestMatchers(HttpMethod.POST, "/auth/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/auth/**").permitAll()

						// Profile endpoints RBAC
						.requestMatchers(HttpMethod.GET, "/profile/**").authenticated()

						// Property endpoints RBAC: allow only SUPERADMIN, ACCOMMODATION_OWNER, CUSTOMER
						// Check both plain and ROLE_ prefixed authorities to avoid mismatch depending
						// on how authorities are granted.
						.requestMatchers(HttpMethod.GET, "/property/**").hasAnyAuthority(
								"SUPERADMIN", "ROLE_SUPERADMIN",
								"ACCOMMODATION_OWNER", "ROLE_ACCOMMODATION_OWNER",
								"CUSTOMER", "ROLE_CUSTOMER")
						.requestMatchers(HttpMethod.POST, "/property/**").hasAnyAuthority(
								"SUPERADMIN", "ROLE_SUPERADMIN",
								"ACCOMMODATION_OWNER", "ROLE_ACCOMMODATION_OWNER")
						.requestMatchers(HttpMethod.PUT, "/property/**").hasAnyAuthority(
								"SUPERADMIN", "ROLE_SUPERADMIN",
								"ACCOMMODATION_OWNER", "ROLE_ACCOMMODATION_OWNER")
						.requestMatchers(HttpMethod.DELETE, "/property/**").hasAnyAuthority(
								"SUPERADMIN", "ROLE_SUPERADMIN",
								"ACCOMMODATION_OWNER", "ROLE_ACCOMMODATION_OWNER")

						// RoomType endpoints RBAC
						.requestMatchers(HttpMethod.GET, "/roomtype/**").hasAnyAuthority(
								"SUPERADMIN", "ROLE_SUPERADMIN",
								"ACCOMMODATION_OWNER", "ROLE_ACCOMMODATION_OWNER",
								"CUSTOMER", "ROLE_CUSTOMER")
						.requestMatchers(HttpMethod.POST, "/roomtype/**").hasAnyAuthority(
								"SUPERADMIN", "ROLE_SUPERADMIN",
								"ACCOMMODATION_OWNER", "ROLE_ACCOMMODATION_OWNER")
						.requestMatchers(HttpMethod.PUT, "/roomtype/**").hasAnyAuthority(
								"SUPERADMIN", "ROLE_SUPERADMIN",
								"ACCOMMODATION_OWNER", "ROLE_ACCOMMODATION_OWNER")
						.requestMatchers(HttpMethod.DELETE, "/roomtype/**").hasAnyAuthority(
								"SUPERADMIN", "ROLE_SUPERADMIN",
								"ACCOMMODATION_OWNER", "ROLE_ACCOMMODATION_OWNER")

						// Room endpoints RBAC
						.requestMatchers(HttpMethod.GET, "/room/**").hasAnyAuthority(
								"SUPERADMIN", "ROLE_SUPERADMIN",
								"ACCOMMODATION_OWNER", "ROLE_ACCOMMODATION_OWNER",
								"CUSTOMER", "ROLE_CUSTOMER")
						.requestMatchers(HttpMethod.POST, "/room/**").hasAnyAuthority(
								"SUPERADMIN", "ROLE_SUPERADMIN",
								"ACCOMMODATION_OWNER", "ROLE_ACCOMMODATION_OWNER")
						.requestMatchers(HttpMethod.PUT, "/room/**").hasAnyAuthority(
								"SUPERADMIN", "ROLE_SUPERADMIN",
								"ACCOMMODATION_OWNER", "ROLE_ACCOMMODATION_OWNER")
						.requestMatchers(HttpMethod.DELETE, "/room/**").hasAnyAuthority(
								"SUPERADMIN", "ROLE_SUPERADMIN",
								"ACCOMMODATION_OWNER", "ROLE_ACCOMMODATION_OWNER")

						// Booking endpoints RBAC
						.requestMatchers(HttpMethod.GET, "/bookings/**").hasAnyAuthority(
								"SUPERADMIN", "ROLE_SUPERADMIN",
								"ACCOMMODATION_OWNER", "ROLE_ACCOMMODATION_OWNER",
								"CUSTOMER", "ROLE_CUSTOMER")
						.requestMatchers(HttpMethod.POST, "/bookings/create/**").hasAnyAuthority("CUSTOMER", "ROLE_CUSTOMER")
						.requestMatchers(HttpMethod.POST, "/bookings/create").hasAnyAuthority("CUSTOMER", "ROLE_CUSTOMER")
						.requestMatchers(HttpMethod.PUT, "/bookings/update").hasAnyAuthority("CUSTOMER", "ROLE_CUSTOMER")
						.requestMatchers(HttpMethod.POST, "/bookings/status/pay").hasAnyAuthority("CUSTOMER", "ROLE_CUSTOMER")
						.requestMatchers(HttpMethod.POST, "/bookings/status/cancel").hasAnyAuthority("CUSTOMER", "ROLE_CUSTOMER")
						.requestMatchers(HttpMethod.POST, "/bookings/status/refund").hasAnyAuthority("CUSTOMER", "ROLE_CUSTOMER")

						// Booking review endpoints RBAC
						.requestMatchers(HttpMethod.GET, "/bookings/reviews/**").hasAnyAuthority(
								"SUPERADMIN", "ROLE_SUPERADMIN",
								"ACCOMMODATION_OWNER", "ROLE_ACCOMMODATION_OWNER",
								"CUSTOMER", "ROLE_CUSTOMER")
						.requestMatchers(HttpMethod.GET, "/bookings/reviews").hasAnyAuthority(
								"SUPERADMIN", "ROLE_SUPERADMIN",
								"ACCOMMODATION_OWNER", "ROLE_ACCOMMODATION_OWNER",
								"CUSTOMER", "ROLE_CUSTOMER")
						.requestMatchers(HttpMethod.POST, "/bookings/reviews/create").hasAnyAuthority("CUSTOMER", "ROLE_CUSTOMER")

						// Statistics endpoints RBAC
						.requestMatchers(HttpMethod.GET, "/statistics/**").hasAnyAuthority(
								"SUPERADMIN", "ROLE_SUPERADMIN",
								"ACCOMMODATION_OWNER", "ROLE_ACCOMMODATION_OWNER")

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

	// ===================== WEB SECURITY (e.g. form login, static resources)
	// =====================
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
