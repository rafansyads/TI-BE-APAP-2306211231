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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletRequest;

import apap.ti._5.accommodation_2306211231_be.models.profile.EndUser;
import apap.ti._5.accommodation_2306211231_be.restdto.BaseRequestDto;
import apap.ti._5.accommodation_2306211231_be.restdto.BaseResponseDto;
import apap.ti._5.accommodation_2306211231_be.restdto.request.auth.LoginRequestDTO;
import apap.ti._5.accommodation_2306211231_be.restdto.request.auth.RegisterRequestDTO;
import apap.ti._5.accommodation_2306211231_be.restdto.request.auth.TokenRefreshRequestDTO;
import apap.ti._5.accommodation_2306211231_be.restdto.request.auth.ForwardRequestDTO;
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
    private final RestTemplate restTemplate;

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
            // expose these headers for this response so browser JS can read them
            headers.set("Access-Control-Expose-Headers", "Refresh-Token, Authorization");
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
                long expMillis = (exp != null) ? exp.getTime()
                        : (System.currentTimeMillis() + jwtUtils.getJwtExpirationMs());
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

        // Refresh token must be supplied in header and validated
        if (refreshHeader == null || refreshHeader.isBlank()) {
            return ResponseUtil.error("Missing Refresh-Token header", HttpStatus.BAD_REQUEST);
        }

        boolean ok = refreshTokenService.validateRefreshToken(refreshHeader, reqUsername);
        if (!ok) {
            return ResponseUtil.error("Invalid or expired refresh token", HttpStatus.UNAUTHORIZED);
        }

        LoginResponseDTO refreshed = null;

        // If Authorization header provided, prefer delegated refresh flow validating
        // existing token
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7).trim();
            if (token != null && !token.isBlank()) {
                refreshed = jwtTokenService.refreshToken(token, reqUsername, reqEmail);
            }
        }

        // If refreshed still null, perform refresh-only flow: issue a new access token
        // based on
        // the username/email and roles associated with the user. This lets clients that
        // only
        // hold a refresh token recover an access token after restart.
        if (refreshed == null) {
            try {
                var user = authRestService.findAggregateByUsername(reqUsername);
                if (user == null) {
                    return ResponseUtil.error("User not found", HttpStatus.UNAUTHORIZED);
                }
                var roles = authRestService.resolveRoles(user);
                String token = jwtUtils.generateJwtToken(user.getId(), user.getUsername(), user.getEmail(),
                        user.getName(), roles);
                refreshed = AuthMapper.toLoginResponseDto(user, roles, token,
                        Instant.now().plusMillis(jwtUtils.getJwtExpirationMs()));
            } catch (Exception e) {
                return ResponseUtil.error("Unable to refresh token", HttpStatus.UNAUTHORIZED);
            }
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
            // expose rotated refresh token and authorization header to browser JS
            headers.set("Access-Control-Expose-Headers", "Refresh-Token, Authorization");
        }

        return ResponseUtil.success(refreshed, "Token refreshed", HttpStatus.OK).toBuilder().headers(headers).build();
    }

    @PostMapping("/forward")
    public ResponseEntity<BaseResponseDto<Map<String, Object>>> forwardToExternal(
            @RequestParam(value = "redirectTo", required = false) String redirectTo,
            @RequestBody BaseRequestDto<ForwardRequestDTO> request,
            HttpServletRequest servletRequest) {
        if (request.getData() == null) {
            return ResponseUtil.error("Missing data object", HttpStatus.BAD_REQUEST);
        }

        ForwardRequestDTO payload = request.getData();
        String targetUrl = payload.getTargetUrl();
        if (targetUrl == null || targetUrl.isBlank()) {
            return ResponseUtil.error("Missing targetUrl in payload", HttpStatus.BAD_REQUEST);
        }
        // Normalize targetUrl: ensure scheme exists (prefer http)
        targetUrl = targetUrl.trim();
        if (!targetUrl.matches("(?i)^https?://.*")) {
            targetUrl = "http://" + targetUrl;
        }

        // Resolve current authenticated user. Prefer SecurityContext if present.
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return ResponseUtil.error("Unauthenticated request", HttpStatus.UNAUTHORIZED);
        }

        var user = authRestService.findAggregateByUsername(auth.getName());
        if (user == null) {
            return ResponseUtil.error("User not found", HttpStatus.UNAUTHORIZED);
        }
        var roles = authRestService.resolveRoles(user);

        // Create an access token for the user to send to the target
        String token = jwtUtils.generateJwtToken(user.getId(), user.getUsername(), user.getEmail(), user.getName(),
                roles);

        // Build JSON payload to send to the downstream backend
        Map<String, Object> forwardBody = new HashMap<>();
        forwardBody.put("accessToken", token);
        if (payload.getParams() != null) {
            forwardBody.put("params", payload.getParams());
        }
        if (redirectTo != null && !redirectTo.isBlank()) {
            String normalizedRedirect = redirectTo.trim();
            if (!normalizedRedirect.matches("(?i)^https?://.*")) {
                normalizedRedirect = "http://" + normalizedRedirect;
            }
            forwardBody.put("redirectTo", normalizedRedirect);
        }

        // capture client metadata
        String ip = servletRequest.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank())
            ip = servletRequest.getRemoteAddr();
        String ua = servletRequest.getHeader("User-Agent");
        forwardBody.put("clientIp", ip);
        forwardBody.put("userAgent", ua);

        // Prepare outgoing headers for the call to the downstream service
        HttpHeaders outHeaders = new HttpHeaders();
        outHeaders.setContentType(MediaType.APPLICATION_JSON);
        String incomingAuth = servletRequest.getHeader("Authorization");
        if (incomingAuth != null && !incomingAuth.isBlank()) {
            // propagate incoming Authorization if present
            outHeaders.set("Authorization", incomingAuth);
        }
        // propagate incoming Refresh-Token header if present so downstream can consume
        // it
        String incomingRefresh = servletRequest.getHeader("Refresh-Token");
        if (incomingRefresh != null && !incomingRefresh.isBlank()) {
            outHeaders.set("Refresh-Token", incomingRefresh);
        }

        ResponseEntity<BaseResponseDto<Map<String, Object>>> downstreamResp;
        try {
            HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(forwardBody, outHeaders);
            downstreamResp = restTemplate.exchange(
                    targetUrl,
                    HttpMethod.POST,
                    httpEntity,
                    new ParameterizedTypeReference<BaseResponseDto<Map<String, Object>>>() {
                    });
        } catch (Exception e) {
            return ResponseUtil.error("Failed to forward request: " + e.getMessage(), HttpStatus.BAD_GATEWAY);
        }

        HttpHeaders responseHeaders = new HttpHeaders();
        if (incomingAuth != null && !incomingAuth.isBlank()) {
            responseHeaders.set("Authorization", incomingAuth);
        }
        responseHeaders.set("X-Forward-Token", token);

        // If downstream provided a Refresh-Token header, expose it manually in our
        // response headers
        var downstreamRefresh = downstreamResp.getHeaders().getFirst("Refresh-Token");
        if (downstreamRefresh != null && !downstreamRefresh.isBlank()) {
            responseHeaders.set("Refresh-Token", downstreamRefresh);
            // expose the header for this response only so frontend JS can read it
            responseHeaders.set("Access-Control-Expose-Headers", "Refresh-Token, Authorization, X-Forward-Token");
        }

        var downstreamBody = downstreamResp.getBody();
        Map<String, Object> data = null;
        if (downstreamBody != null) {
            data = downstreamBody.getData();
        }

        HttpStatus status = HttpStatus.resolve(downstreamResp.getStatusCode().value());
        if (status == null)
            status = HttpStatus.FOUND;
        return ResponseUtil.success(data, "Forward response", status).toBuilder().headers(responseHeaders).build();
    }

    private String htmlEscape(String s) {
        if (s == null)
            return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#x27;");
    }
}
