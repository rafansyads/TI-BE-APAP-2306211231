package apap.ti._5.accommodation_2306211231_be.security.service;

import apap.ti._5.accommodation_2306211231_be.models.profile.EndUser;
import apap.ti._5.accommodation_2306211231_be.restdto.response.auth.LoginResponseDTO;
import apap.ti._5.accommodation_2306211231_be.restmapper.AuthMapper;
import apap.ti._5.accommodation_2306211231_be.restservice.AuthRestService;
import apap.ti._5.accommodation_2306211231_be.security.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

		private final JwtUtils jwtUtils;
		private final AuthRestService authRestService;
    private final JwtTokenBlacklist jwtTokenBlacklist;

		/**
		 * Validate the provided token, ensure the referenced user still exists and is authorised,
		 * and return a fresh token (and metadata) for that user.
		 *
		 * @param token existing JWT token
		 * @return LoginResponseDTO with a new token and expiration, or null if validation failed / user not found
		 */
		public LoginResponseDTO refreshToken(String token, String reqUsername, String reqEmail) {
			if (token == null || token.isBlank()) return null;

			if (reqUsername == null || reqUsername.isBlank() || reqEmail == null || reqEmail.isBlank()) return null;

			// Use the username from the request (the refresh token validation should be performed
			// before calling this service). Ensure the user still exists and the email matches.
			EndUser user = authRestService.findAggregateByUsername(reqUsername);
			if (user == null) return null;
			if (!reqEmail.equalsIgnoreCase(user.getEmail())) return null;

			List<String> currentRoles = authRestService.resolveRoles(user);

			// Generate a new token using the current user info and roles
			String newToken = jwtUtils.generateJwtToken(user.getId(), user.getUsername(), user.getEmail(), user.getName(), currentRoles);

			// Revoke the old token immediately by recording it in the blacklist until its natural expiration
			try {
				java.util.Date oldExp = jwtUtils.getExpirationFromJwtToken(token);
				long expMillis = (oldExp != null) ? oldExp.getTime() : (System.currentTimeMillis() + jwtUtils.getJwtExpirationMs());
				jwtTokenBlacklist.revoke(token, expMillis);
			} catch (Exception ignored) {
				// if anything goes wrong revoking, we still return the new token
			}

			return AuthMapper.toLoginResponseDto(user, currentRoles, newToken, Instant.now().plusMillis(jwtUtils.getJwtExpirationMs()));
		}

}
