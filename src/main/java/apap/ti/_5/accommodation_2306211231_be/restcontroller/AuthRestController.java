package apap.ti._5.accommodation_2306211231_be.restcontroller;

import java.time.Instant;
import java.util.List;
import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpHeaders;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletRequest;

import apap.ti._5.accommodation_2306211231_be.models.profile.EndUser;
import apap.ti._5.accommodation_2306211231_be.restdto.BaseRequestDto;
import apap.ti._5.accommodation_2306211231_be.restdto.BaseResponseDto;
import apap.ti._5.accommodation_2306211231_be.restdto.request.auth.LoginRequestDTO;
import apap.ti._5.accommodation_2306211231_be.restdto.request.auth.RegisterRequestDTO;
import apap.ti._5.accommodation_2306211231_be.restdto.request.auth.TokenRefreshRequestDTO;
import apap.ti._5.accommodation_2306211231_be.restdto.response.auth.LoginResponseDTO;
import apap.ti._5.accommodation_2306211231_be.restdto.response.auth.LogoutResponseDTO;
import apap.ti._5.accommodation_2306211231_be.restdto.response.auth.RegisterResponseDTO;
import apap.ti._5.accommodation_2306211231_be.restmapper.AuthMapper;
import apap.ti._5.accommodation_2306211231_be.restservice.AuthRestService;
import apap.ti._5.accommodation_2306211231_be.security.jwt.JwtUtils;
import apap.ti._5.accommodation_2306211231_be.security.service.JwtTokenService;
import apap.ti._5.accommodation_2306211231_be.util.ResponseUtil;
import apap.ti._5.accommodation_2306211231_be.security.service.RefreshTokenService;
import apap.ti._5.accommodation_2306211231_be.security.service.JwtTokenBlacklist;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final AuthRestService authRestService;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenBlacklist jwtTokenBlacklist;

    @PostMapping("/login")
    public ResponseEntity<BaseResponseDto<LoginResponseDTO>> login(
            @RequestBody BaseRequestDto<LoginRequestDTO> request,
            HttpServletRequest servletRequest) {
        try {
            if (request.getData() == null) {
                return ResponseUtil.error("Missing data object", HttpStatus.BAD_REQUEST);
            }
            LoginRequestDTO payload = request.getData();
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(payload.getUsername(), payload.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Resolve user & role for JWT claims
            EndUser user = authRestService.findAggregateByUsername(payload.getUsername());
            List<String> roles = authRestService.resolveRoles(user);

            String token = jwtUtils.generateJwtToken(user.getId(), user.getUsername(), user.getEmail(), user.getName(),
                    roles);
            LoginResponseDTO data = AuthMapper.toLoginResponseDto(user, roles, token,
                    Instant.now().plusMillis(jwtUtils.getJwtExpirationMs()));
            // capture client metadata for refresh token
            String ip = servletRequest.getHeader("X-Forwarded-For");
            if (ip == null || ip.isBlank())
                ip = servletRequest.getRemoteAddr();
            String ua = servletRequest.getHeader("User-Agent");
            
            // create refresh token and return both tokens in headers
            String refreshToken = refreshTokenService.createRefreshToken(user.getUsername(), ip, ua);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.set("Refresh-Token", refreshToken);
            return ResponseUtil.success(
                    data,
                    "Login success",
                    HttpStatus.OK)
                    .toBuilder().headers(headers).build();
        } catch (BadCredentialsException ex) {
            return ResponseUtil.error("Invalid username or password", HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<BaseResponseDto<RegisterResponseDTO>> register(
            @RequestBody BaseRequestDto<RegisterRequestDTO> request) {
        if (request.getData() == null) {
            return ResponseUtil.error("Missing data object", HttpStatus.BAD_REQUEST);
        }
        RegisterRequestDTO payload = request.getData();
        if ("SUPERADMIN".equalsIgnoreCase(payload.getRole())) {
            return ResponseUtil.error("Cannot self-register SUPERADMIN role", HttpStatus.FORBIDDEN);
        }
        if (authRestService.existsUsername(payload.getUsername())) {
            return ResponseUtil.error("Username already taken", HttpStatus.CONFLICT);
        }
        if (authRestService.existsEmail(payload.getEmail())) {
            return ResponseUtil.error("Email already taken", HttpStatus.CONFLICT);
        }
        EndUser created = authRestService.register(payload);

        RegisterResponseDTO data = AuthMapper.toRegisterResponseDto(created,
                authRestService.resolveRoles(created).get(0), Instant.now());
        return ResponseUtil.success(data, "Register success", HttpStatus.CREATED).toBuilder().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<BaseResponseDto<LogoutResponseDTO>> logout(HttpServletRequest servletRequest) {
        // revoke all refresh tokens for the authenticated user (if present)
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            try {
                refreshTokenService.revokeAllTokensForUser(auth.getName());
            } catch (Exception ignored) {
            }
        }
        SecurityContextHolder.clearContext();

        // Blacklist the JWT token provided in Authorization header (if present)
        String authorization = servletRequest.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7).trim();
            try {
                Date exp = jwtUtils.getExpirationFromJwtToken(token);
                long expMillis = (exp != null) ? exp.getTime() : (System.currentTimeMillis() + jwtUtils.getJwtExpirationMs());
                jwtTokenBlacklist.revoke(token, expMillis);
            } catch (Exception ignored) {
                // if anything goes wrong revoking, we still proceed with logout
            }
        }
        LogoutResponseDTO data = AuthMapper.toLogoutResponseDto("Logged out (client must discard token)",
                Instant.now());
        return ResponseUtil.success(data, "Logout success", HttpStatus.OK).toBuilder().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<BaseResponseDto<LoginResponseDTO>> refreshToken(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "Refresh-Token", required = false) String refreshHeader,
            @RequestBody BaseRequestDto<TokenRefreshRequestDTO> request,
            HttpServletRequest servletRequest) {
        if (request.getData() == null) {
            return ResponseUtil.error("Missing data object", HttpStatus.BAD_REQUEST);
        }

        // Extract username/email from request body
        TokenRefreshRequestDTO dto = request.getData();
        String reqUsername = dto.getUsername();
        String reqEmail = dto.getEmail();

        if (reqUsername == null || reqUsername.isBlank() || reqEmail == null || reqEmail.isBlank()) {
            return ResponseUtil.error("Missing username or email in request", HttpStatus.BAD_REQUEST);
        }

        // Extract token from Authorization header (Bearer token)
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseUtil.error("Missing or invalid Authorization header", HttpStatus.BAD_REQUEST);
        }

        String token = authorization.substring(7).trim();
        if (token == null || token.isBlank()) {
            return ResponseUtil.error("Missing token", HttpStatus.BAD_REQUEST);
        }

        // Refresh token must be supplied in header and validated
        if (refreshHeader == null || refreshHeader.isBlank()) {
            return ResponseUtil.error("Missing Refresh-Token header", HttpStatus.BAD_REQUEST);
        }

        boolean ok = refreshTokenService.validateRefreshToken(refreshHeader, reqUsername);
        if (!ok) {
            return ResponseUtil.error("Invalid or expired refresh token", HttpStatus.UNAUTHORIZED);
        }

        // Delegate to JwtTokenService which validates token, checks user and issues a
        // new token
        LoginResponseDTO refreshed = jwtTokenService.refreshToken(token, reqUsername, reqEmail);
        if (refreshed == null) {
            return ResponseUtil.error("Invalid or expired token, or user not authorized", HttpStatus.UNAUTHORIZED);
        }

        // Using LoginResponseDTO because it contains token and expiry info
        HttpHeaders headers = new HttpHeaders();
        if (refreshed.getToken() != null && !refreshed.getToken().isBlank()) {
            headers.set("Authorization", "Bearer " + refreshed.getToken());
        }
        // capture client metadata
        String ip = servletRequest.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank())
            ip = servletRequest.getRemoteAddr();
        String ua = servletRequest.getHeader("User-Agent");

        // rotate refresh token (try to rotate; if rotation fails, create a new one)
        String newRefresh = null;
        try {
            newRefresh = refreshTokenService.rotateRefreshToken(refreshHeader, reqUsername, ip, ua);
        } catch (Exception ignored) {
        }
        if (newRefresh == null) {
            try {
                newRefresh = refreshTokenService.createRefreshToken(reqUsername, ip, ua);
            } catch (Exception ignored) {
            }
        }
        if (newRefresh != null) {
            headers.set("Refresh-Token", newRefresh);
        }

        return ResponseUtil.success(refreshed, "Token refreshed", HttpStatus.OK).toBuilder().headers(headers).build();
    }
}
