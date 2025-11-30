package apap.ti._5.accommodation_2306211231_be.restcontroller;

import apap.ti._5.accommodation_2306211231_be.models.profile.Customer;
import apap.ti._5.accommodation_2306211231_be.restdto.BaseRequestDto;
import apap.ti._5.accommodation_2306211231_be.restdto.request.auth.ForwardRequestDTO;
import apap.ti._5.accommodation_2306211231_be.restdto.request.auth.LoginRequestDTO;
import apap.ti._5.accommodation_2306211231_be.restdto.request.auth.TokenRefreshRequestDTO;
import apap.ti._5.accommodation_2306211231_be.restservice.AuthRestService;
import apap.ti._5.accommodation_2306211231_be.security.jwt.JwtUtils;
import apap.ti._5.accommodation_2306211231_be.security.service.JwtTokenService;
import apap.ti._5.accommodation_2306211231_be.security.service.RefreshTokenService;
import apap.ti._5.accommodation_2306211231_be.security.service.JwtTokenBlacklist;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthRestControllerMoreUnitTests {

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void login_success_setsAuthorizationAndRefreshHeaders() {
        AuthenticationManager authManager = mock(AuthenticationManager.class);
        JwtUtils jwtUtils = mock(JwtUtils.class);
        AuthRestService authService = mock(AuthRestService.class);
        JwtTokenService jwtTokenService = mock(JwtTokenService.class);
        RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
        JwtTokenBlacklist blacklist = mock(JwtTokenBlacklist.class);

        Authentication auth = mock(Authentication.class);
        when(authManager.authenticate(any())).thenReturn(auth);

        Customer c = new Customer();
        c.setId(java.util.UUID.randomUUID());
        c.setUsername("u1");
        c.setEmail("e@x.com");
        c.setName("N");
        when(authService.findAggregateByUsername("u1")).thenReturn(c);
        when(authService.resolveRoles(any())).thenReturn(List.of("ROLE_CUSTOMER"));

        when(jwtUtils.generateJwtToken(any(), anyString(), anyString(), anyString(), anyList())).thenReturn("tok");
        when(jwtUtils.getJwtExpirationMs()).thenReturn(1000);
        when(refreshTokenService.createRefreshToken(eq("u1"), anyString(), anyString())).thenReturn("rTok");

        var controller = new AuthRestController(authManager, jwtUtils, authService, jwtTokenService, refreshTokenService, blacklist, mock(org.springframework.web.client.RestTemplate.class));

        LoginRequestDTO payload = new LoginRequestDTO();
        payload.setUsername("u1");
        payload.setPassword("p");
        BaseRequestDto<LoginRequestDTO> req = new BaseRequestDto<>();
        req.setData(payload);

        MockHttpServletRequest servletReq = new MockHttpServletRequest();
        servletReq.setRemoteAddr("1.2.3.4");
        servletReq.addHeader("User-Agent", "ua");

        ResponseEntity<?> resp = controller.login(req, servletReq);
        assertEquals(200, resp.getStatusCodeValue());
        assertNotNull(resp.getHeaders().getFirst("Authorization"));
        assertNotNull(resp.getHeaders().getFirst("Refresh-Token"));
    }

    @Test
    void login_badCredentials_returns401() {
        AuthenticationManager authManager = mock(AuthenticationManager.class);
        when(authManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));
        var controller = new AuthRestController(authManager, mock(JwtUtils.class), mock(AuthRestService.class), mock(JwtTokenService.class), mock(RefreshTokenService.class), mock(JwtTokenBlacklist.class), mock(org.springframework.web.client.RestTemplate.class));

        LoginRequestDTO payload = new LoginRequestDTO();
        payload.setUsername("u2");
        payload.setPassword("p");
        BaseRequestDto<LoginRequestDTO> req = new BaseRequestDto<>();
        req.setData(payload);

        MockHttpServletRequest servletReq = new MockHttpServletRequest();

        ResponseEntity<?> resp = controller.login(req, servletReq);
        assertEquals(401, resp.getStatusCodeValue());
    }

    @Test
    void logout_withAuthorization_revokesTokenAndReturns200() {
        JwtUtils jwtUtils = mock(JwtUtils.class);
        AuthRestService authService = mock(AuthRestService.class);
        RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
        JwtTokenBlacklist blacklist = mock(JwtTokenBlacklist.class);

        var controller = new AuthRestController(mock(AuthenticationManager.class), jwtUtils, authService, mock(JwtTokenService.class), refreshTokenService, blacklist, mock(org.springframework.web.client.RestTemplate.class));

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("someone");
        SecurityContextHolder.getContext().setAuthentication(auth);

        MockHttpServletRequest servletReq = new MockHttpServletRequest();
        servletReq.addHeader("Authorization", "Bearer sometoken");
        when(jwtUtils.getExpirationFromJwtToken("sometoken")).thenReturn(new Date(System.currentTimeMillis() + 10_000));

        ResponseEntity<?> resp = controller.logout(servletReq);
        assertEquals(200, resp.getStatusCodeValue());
        verify(blacklist, atLeastOnce()).revoke(eq("sometoken"), anyLong());
    }

    @Test
    void refresh_missingRefreshHeader_returns400() {
        var controller = new AuthRestController(mock(AuthenticationManager.class), mock(JwtUtils.class), mock(AuthRestService.class), mock(JwtTokenService.class), mock(RefreshTokenService.class), mock(JwtTokenBlacklist.class), mock(org.springframework.web.client.RestTemplate.class));

        TokenRefreshRequestDTO dto = new TokenRefreshRequestDTO();
        dto.setUsername("u"); dto.setEmail("e@x.com");
        BaseRequestDto<TokenRefreshRequestDTO> req = new BaseRequestDto<>();
        req.setData(dto);

        MockHttpServletRequest servletReq = new MockHttpServletRequest();

        ResponseEntity<?> resp = controller.refreshToken(null, null, req, servletReq);
        assertEquals(400, resp.getStatusCodeValue());
    }

    @Test
    void refresh_invalidRefreshToken_returns401() {
        RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
        when(refreshTokenService.validateRefreshToken(eq("bad"), eq("u"))).thenReturn(false);

        var controller = new AuthRestController(mock(AuthenticationManager.class), mock(JwtUtils.class), mock(AuthRestService.class), mock(JwtTokenService.class), refreshTokenService, mock(JwtTokenBlacklist.class), mock(org.springframework.web.client.RestTemplate.class));

        TokenRefreshRequestDTO dto = new TokenRefreshRequestDTO();
        dto.setUsername("u"); dto.setEmail("e@x.com");
        BaseRequestDto<TokenRefreshRequestDTO> req = new BaseRequestDto<>();
        req.setData(dto);

        MockHttpServletRequest servletReq = new MockHttpServletRequest();

        ResponseEntity<?> resp = controller.refreshToken(null, "bad", req, servletReq);
        assertEquals(401, resp.getStatusCodeValue());
    }

    @Test
    void forward_missingTargetUrl_and_unauthenticated_cases() {
        var controller = new AuthRestController(mock(AuthenticationManager.class), mock(JwtUtils.class), mock(AuthRestService.class), mock(JwtTokenService.class), mock(RefreshTokenService.class), mock(JwtTokenBlacklist.class), mock(org.springframework.web.client.RestTemplate.class));

        // missing data
        BaseRequestDto<ForwardRequestDTO> req1 = new BaseRequestDto<>();
        MockHttpServletRequest servletReq = new MockHttpServletRequest();
        ResponseEntity<?> r1 = controller.forwardToExternal(null, req1, servletReq);
        assertEquals(400, r1.getStatusCodeValue());

        // missing targetUrl
        ForwardRequestDTO f = new ForwardRequestDTO();
        BaseRequestDto<ForwardRequestDTO> req2 = new BaseRequestDto<>(); req2.setData(f);
        ResponseEntity<?> r2 = controller.forwardToExternal(null, req2, servletReq);
        assertEquals(400, r2.getStatusCodeValue());

        // unauthenticated
        f.setTargetUrl("http://example.com");
        ResponseEntity<?> r3 = controller.forwardToExternal(null, req2, servletReq);
        assertEquals(401, r3.getStatusCodeValue());
    }
}
