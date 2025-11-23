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
import java.util.UUID;

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

				boolean valid = jwtUtils.validateJwtToken(token);
				if (!valid) return null;

				// Extract claims from the token
				String idStr = jwtUtils.getIdFromJwtToken(token);
				String tokenUsername = jwtUtils.getUserNameFromJwtToken(token);
				String tokenEmail = jwtUtils.getEmailFromJwtToken(token);

				// Verify provided username/email match the token claims (defense-in-depth)
				if (reqUsername == null || reqEmail == null) return null;
				if (!reqUsername.equals(tokenUsername) || !reqEmail.equalsIgnoreCase(tokenEmail)) return null;

				// Ensure user still exists and resolve current roles
				EndUser user = authRestService.findAggregateByUsername(tokenUsername);
				if (user == null) return null;

				List<String> currentRoles = authRestService.resolveRoles(user);

				UUID id;
				try {
						id = UUID.fromString(idStr);
					} catch (Exception ex) {
								// if id claim is malformed, fallback to user's id if available
						try {
								id = user.getId();
						} catch (Exception e) {
								return null;
						}
				}

				// Generate a new token using the (possibly updated) roles and user info
				String newToken = jwtUtils.generateJwtToken(id, user.getUsername(), user.getEmail(), user.getName(), currentRoles);

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
