package apap.ti._5.accommodation_2306211231_be.restcontroller;

import apap.ti._5.accommodation_2306211231_be.models.profile.Customer;
import apap.ti._5.accommodation_2306211231_be.models.profile.EndUser;
import apap.ti._5.accommodation_2306211231_be.restdto.BaseRequestDto;
import apap.ti._5.accommodation_2306211231_be.restdto.BaseResponseDto;
import apap.ti._5.accommodation_2306211231_be.restdto.request.auth.*;
import apap.ti._5.accommodation_2306211231_be.restdto.response.auth.*;
import apap.ti._5.accommodation_2306211231_be.restservice.AuthRestService;
import apap.ti._5.accommodation_2306211231_be.security.jwt.JwtUtils;
import apap.ti._5.accommodation_2306211231_be.security.service.JwtTokenBlacklist;
import apap.ti._5.accommodation_2306211231_be.security.service.JwtTokenService;
import apap.ti._5.accommodation_2306211231_be.security.service.RefreshTokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthRestControllerBranchTests {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtils jwtUtils;
    @Mock private AuthRestService authRestService;
    @Mock private JwtTokenService jwtTokenService;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private JwtTokenBlacklist jwtTokenBlacklist;
    @Mock private RestTemplate restTemplate;

    private AuthRestController controller;
    private SecurityContext originalContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new AuthRestController(
            authenticationManager, jwtUtils, authRestService, 
            jwtTokenService, refreshTokenService, jwtTokenBlacklist, restTemplate
        );
        originalContext = SecurityContextHolder.getContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.setContext(originalContext);
    }

    @Test
    void login_missingData_returns400() {
        BaseRequestDto<LoginRequestDTO> req = new BaseRequestDto<>();
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();

        ResponseEntity<BaseResponseDto<LoginResponseDTO>> response = controller.login(req, servletRequest);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void login_badCredentials_returns401() {
        LoginRequestDTO payload = new LoginRequestDTO();
        payload.setUsername("user");
        payload.setPassword("wrong");
        
        BaseRequestDto<LoginRequestDTO> req = new BaseRequestDto<>();
        req.setData(payload);
        
        when(authenticationManager.authenticate(any()))
            .thenThrow(new BadCredentialsException("Bad credentials"));
        
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();

        ResponseEntity<BaseResponseDto<LoginResponseDTO>> response = controller.login(req, servletRequest);

        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void login_success_returnsTokens() {
        LoginRequestDTO payload = new LoginRequestDTO();
        payload.setUsername("user");
        payload.setPassword("pass");
        
        BaseRequestDto<LoginRequestDTO> req = new BaseRequestDto<>();
        req.setData(payload);
        
        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        
        Customer user = new Customer();
        user.setId(UUID.randomUUID());
        user.setUsername("user");
        user.setEmail("user@test.com");
        user.setName("User Name");
        
        when(authRestService.findAggregateByUsername("user")).thenReturn(user);
        when(authRestService.resolveRoles(user)).thenReturn(List.of("ROLE_CUSTOMER"));
        when(jwtUtils.generateJwtToken(any(), anyString(), anyString(), anyString(), anyList()))
            .thenReturn("jwt-token");
        when(jwtUtils.getJwtExpirationMs()).thenReturn(3600000);
        when(refreshTokenService.createRefreshToken(anyString(), anyString(), anyString()))
            .thenReturn("refresh-token");
        
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.addHeader("X-Forwarded-For", "192.168.1.1");
        servletRequest.addHeader("User-Agent", "Mozilla/5.0");

        ResponseEntity<BaseResponseDto<LoginResponseDTO>> response = controller.login(req, servletRequest);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getHeaders().get("Authorization"));
        assertNotNull(response.getHeaders().get("Refresh-Token"));
    }

    @Test
    void login_noXForwardedFor_usesRemoteAddr() {
        LoginRequestDTO payload = new LoginRequestDTO();
        payload.setUsername("user");
        payload.setPassword("pass");
        
        BaseRequestDto<LoginRequestDTO> req = new BaseRequestDto<>();
        req.setData(payload);
        
        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
        
        Customer user = new Customer();
        user.setId(UUID.randomUUID());
        user.setUsername("user");
        user.setEmail("user@test.com");
        
        when(authRestService.findAggregateByUsername("user")).thenReturn(user);
        when(authRestService.resolveRoles(user)).thenReturn(List.of("ROLE_CUSTOMER"));
        when(jwtUtils.generateJwtToken(any(), anyString(), anyString(), any(), anyList()))
            .thenReturn("jwt-token");
        when(jwtUtils.getJwtExpirationMs()).thenReturn(3600000);
        when(refreshTokenService.createRefreshToken(anyString(), anyString(), anyString()))
            .thenReturn("refresh-token");
        
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setRemoteAddr("10.0.0.1");

        ResponseEntity<BaseResponseDto<LoginResponseDTO>> response = controller.login(req, servletRequest);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void register_superadminRole_returnsForbidden() {
        RegisterRequestDTO payload = new RegisterRequestDTO();
        payload.setRole("SUPERADMIN");
        
        BaseRequestDto<RegisterRequestDTO> req = new BaseRequestDto<>();
        req.setData(payload);

        ResponseEntity<BaseResponseDto<RegisterResponseDTO>> response = controller.register(req);

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void register_emailTaken_returnsConflict() {
        RegisterRequestDTO payload = new RegisterRequestDTO();
        payload.setUsername("newuser");
        payload.setEmail("taken@test.com");
        
        BaseRequestDto<RegisterRequestDTO> req = new BaseRequestDto<>();
        req.setData(payload);
        
        when(authRestService.existsUsername("newuser")).thenReturn(false);
        when(authRestService.existsEmail("taken@test.com")).thenReturn(true);

        ResponseEntity<BaseResponseDto<RegisterResponseDTO>> response = controller.register(req);

        assertEquals(409, response.getStatusCode().value());
    }

    @Test
    void logout_withAuthAndToken_blacklistsToken() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user");
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
        
        when(jwtUtils.getExpirationFromJwtToken("token123")).thenReturn(new Date(System.currentTimeMillis() + 60000));
        when(jwtUtils.getJwtExpirationMs()).thenReturn(3600000);
        
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.addHeader("Authorization", "Bearer token123");

        ResponseEntity<BaseResponseDto<LogoutResponseDTO>> response = controller.logout(servletRequest);

        assertEquals(200, response.getStatusCode().value());
        verify(refreshTokenService).revokeAllTokensForUser("user");
        verify(jwtTokenBlacklist).revoke(eq("token123"), anyLong());
    }

    @Test
    void logout_revokeThrowsException_stillSucceeds() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user");
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
        
        doThrow(new RuntimeException("Error")).when(refreshTokenService).revokeAllTokensForUser(anyString());
        
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();

        ResponseEntity<BaseResponseDto<LogoutResponseDTO>> response = controller.logout(servletRequest);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void logout_blacklistThrowsException_stillSucceeds() {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.addHeader("Authorization", "Bearer token123");
        
        when(jwtUtils.getExpirationFromJwtToken("token123")).thenThrow(new RuntimeException("Error"));

        ResponseEntity<BaseResponseDto<LogoutResponseDTO>> response = controller.logout(servletRequest);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void refresh_missingData_returns400() {
        BaseRequestDto<TokenRefreshRequestDTO> req = new BaseRequestDto<>();
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();

        ResponseEntity<BaseResponseDto<LoginResponseDTO>> response = 
            controller.refreshToken(null, null, req, servletRequest);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void refresh_missingUsername_returns400() {
        TokenRefreshRequestDTO dto = new TokenRefreshRequestDTO();
        dto.setEmail("email@test.com");
        
        BaseRequestDto<TokenRefreshRequestDTO> req = new BaseRequestDto<>();
        req.setData(dto);
        
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();

        ResponseEntity<BaseResponseDto<LoginResponseDTO>> response = 
            controller.refreshToken(null, null, req, servletRequest);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void refresh_missingRefreshHeader_returns400() {
        TokenRefreshRequestDTO dto = new TokenRefreshRequestDTO();
        dto.setUsername("user");
        dto.setEmail("email@test.com");
        
        BaseRequestDto<TokenRefreshRequestDTO> req = new BaseRequestDto<>();
        req.setData(dto);
        
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();

        ResponseEntity<BaseResponseDto<LoginResponseDTO>> response = 
            controller.refreshToken(null, null, req, servletRequest);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void refresh_invalidRefreshToken_returns401() {
        TokenRefreshRequestDTO dto = new TokenRefreshRequestDTO();
        dto.setUsername("user");
        dto.setEmail("email@test.com");
        
        BaseRequestDto<TokenRefreshRequestDTO> req = new BaseRequestDto<>();
        req.setData(dto);
        
        when(refreshTokenService.validateRefreshToken("refresh-token", "user")).thenReturn(false);
        
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();

        ResponseEntity<BaseResponseDto<LoginResponseDTO>> response = 
            controller.refreshToken(null, "refresh-token", req, servletRequest);

        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void refresh_withAuthorizationHeader_usesJwtTokenService() {
        TokenRefreshRequestDTO dto = new TokenRefreshRequestDTO();
        dto.setUsername("user");
        dto.setEmail("email@test.com");
        
        BaseRequestDto<TokenRefreshRequestDTO> req = new BaseRequestDto<>();
        req.setData(dto);
        
        when(refreshTokenService.validateRefreshToken("refresh-token", "user")).thenReturn(true);
        
        LoginResponseDTO loginDto = LoginResponseDTO.builder()
            .token("new-token")
            .id("id")
            .username("user")
            .name("name")
            .email("email@test.com")
            .roles(List.of("ROLE_CUSTOMER"))
            .build();
        when(jwtTokenService.refreshToken("old-token", "user", "email@test.com")).thenReturn(loginDto);
        when(refreshTokenService.rotateRefreshToken(anyString(), anyString(), anyString(), anyString()))
            .thenReturn("new-refresh-token");
        
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();

        ResponseEntity<BaseResponseDto<LoginResponseDTO>> response = 
            controller.refreshToken("Bearer old-token", "refresh-token", req, servletRequest);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void refresh_userNotFound_returns401() {
        TokenRefreshRequestDTO dto = new TokenRefreshRequestDTO();
        dto.setUsername("user");
        dto.setEmail("email@test.com");
        
        BaseRequestDto<TokenRefreshRequestDTO> req = new BaseRequestDto<>();
        req.setData(dto);
        
        when(refreshTokenService.validateRefreshToken("refresh-token", "user")).thenReturn(true);
        when(jwtTokenService.refreshToken(anyString(), anyString(), anyString())).thenReturn(null);
        when(authRestService.findAggregateByUsername("user")).thenReturn(null);
        
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();

        ResponseEntity<BaseResponseDto<LoginResponseDTO>> response = 
            controller.refreshToken("Bearer old-token", "refresh-token", req, servletRequest);

        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void refresh_rotationFails_createsNewToken() {
        TokenRefreshRequestDTO dto = new TokenRefreshRequestDTO();
        dto.setUsername("user");
        dto.setEmail("email@test.com");
        
        BaseRequestDto<TokenRefreshRequestDTO> req = new BaseRequestDto<>();
        req.setData(dto);
        
        when(refreshTokenService.validateRefreshToken("refresh-token", "user")).thenReturn(true);
        
        LoginResponseDTO loginDto = LoginResponseDTO.builder()
            .token("new-token")
            .id("id")
            .username("user")
            .name("name")
            .email("email@test.com")
            .roles(List.of("ROLE_CUSTOMER"))
            .build();
        when(jwtTokenService.refreshToken("old-token", "user", "email@test.com")).thenReturn(loginDto);
        when(refreshTokenService.rotateRefreshToken(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(null);
        when(refreshTokenService.createRefreshToken(anyString(), anyString(), anyString()))
            .thenReturn("new-refresh-token");
        
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();

        ResponseEntity<BaseResponseDto<LoginResponseDTO>> response = 
            controller.refreshToken("Bearer old-token", "refresh-token", req, servletRequest);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void forward_missingData_returns400() {
        BaseRequestDto<ForwardRequestDTO> req = new BaseRequestDto<>();
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();

        ResponseEntity<BaseResponseDto<Map<String, Object>>> response = 
            controller.forwardToExternal(null, req, servletRequest);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void forward_missingTargetUrl_returns400() {
        ForwardRequestDTO dto = new ForwardRequestDTO();
        BaseRequestDto<ForwardRequestDTO> req = new BaseRequestDto<>();
        req.setData(dto);
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();

        ResponseEntity<BaseResponseDto<Map<String, Object>>> response = 
            controller.forwardToExternal(null, req, servletRequest);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void forward_noAuth_returns401() {
        ForwardRequestDTO dto = new ForwardRequestDTO();
        dto.setTargetUrl("http://example.com");
        BaseRequestDto<ForwardRequestDTO> req = new BaseRequestDto<>();
        req.setData(dto);
        
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(ctx);
        
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();

        ResponseEntity<BaseResponseDto<Map<String, Object>>> response = 
            controller.forwardToExternal(null, req, servletRequest);

        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void forward_userNotFound_returns401() {
        ForwardRequestDTO dto = new ForwardRequestDTO();
        dto.setTargetUrl("http://example.com");
        BaseRequestDto<ForwardRequestDTO> req = new BaseRequestDto<>();
        req.setData(dto);
        
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user");
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
        
        when(authRestService.findAggregateByUsername("user")).thenReturn(null);
        
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();

        ResponseEntity<BaseResponseDto<Map<String, Object>>> response = 
            controller.forwardToExternal(null, req, servletRequest);

        assertEquals(401, response.getStatusCode().value());
    }
}
