package apap.ti._5.accommodation_2306211231_be.security.service;

import apap.ti._5.accommodation_2306211231_be.models.profile.Customer;
import apap.ti._5.accommodation_2306211231_be.models.profile.EndUser;
import apap.ti._5.accommodation_2306211231_be.restdto.response.auth.LoginResponseDTO;
import apap.ti._5.accommodation_2306211231_be.restservice.AuthRestService;
import apap.ti._5.accommodation_2306211231_be.security.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class JwtTokenServiceTest {

    @Mock
    private JwtUtils jwtUtils;
    
    @Mock
    private AuthRestService authRestService;
    
    @Mock
    private JwtTokenBlacklist jwtTokenBlacklist;

    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtTokenService = new JwtTokenService(jwtUtils, authRestService, jwtTokenBlacklist);
    }

    @Test
    void refreshToken_nullToken_returnsNull() {
        LoginResponseDTO result = jwtTokenService.refreshToken(null, "user", "email@test.com");
        assertNull(result);
    }

    @Test
    void refreshToken_blankToken_returnsNull() {
        LoginResponseDTO result = jwtTokenService.refreshToken("   ", "user", "email@test.com");
        assertNull(result);
    }

    @Test
    void refreshToken_nullUsername_returnsNull() {
        LoginResponseDTO result = jwtTokenService.refreshToken("token", null, "email@test.com");
        assertNull(result);
    }

    @Test
    void refreshToken_blankUsername_returnsNull() {
        LoginResponseDTO result = jwtTokenService.refreshToken("token", "  ", "email@test.com");
        assertNull(result);
    }

    @Test
    void refreshToken_nullEmail_returnsNull() {
        LoginResponseDTO result = jwtTokenService.refreshToken("token", "user", null);
        assertNull(result);
    }

    @Test
    void refreshToken_blankEmail_returnsNull() {
        LoginResponseDTO result = jwtTokenService.refreshToken("token", "user", "  ");
        assertNull(result);
    }

    @Test
    void refreshToken_userNotFound_returnsNull() {
        when(authRestService.findAggregateByUsername("user")).thenReturn(null);
        
        LoginResponseDTO result = jwtTokenService.refreshToken("token", "user", "email@test.com");
        assertNull(result);
    }

    @Test
    void refreshToken_emailMismatch_returnsNull() {
        Customer user = new Customer();
        user.setUsername("user");
        user.setEmail("different@test.com");
        when(authRestService.findAggregateByUsername("user")).thenReturn(user);
        
        LoginResponseDTO result = jwtTokenService.refreshToken("token", "user", "email@test.com");
        assertNull(result);
    }

    @Test
    void refreshToken_success_returnsNewToken() {
        Customer user = new Customer();
        user.setId(UUID.randomUUID());
        user.setUsername("user");
        user.setEmail("email@test.com");
        user.setName("Test User");
        
        when(authRestService.findAggregateByUsername("user")).thenReturn(user);
        when(authRestService.resolveRoles(user)).thenReturn(List.of("ROLE_CUSTOMER"));
        when(jwtUtils.generateJwtToken(any(), eq("user"), eq("email@test.com"), eq("Test User"), anyList()))
            .thenReturn("new-token");
        when(jwtUtils.getExpirationFromJwtToken("old-token")).thenReturn(new Date(System.currentTimeMillis() + 60000));
        when(jwtUtils.getJwtExpirationMs()).thenReturn(3600000);
        
        LoginResponseDTO result = jwtTokenService.refreshToken("old-token", "user", "email@test.com");
        
        assertNotNull(result);
        assertEquals("new-token", result.getToken());
        verify(jwtTokenBlacklist).revoke(eq("old-token"), anyLong());
    }

    @Test
    void refreshToken_emailCaseInsensitive_works() {
        Customer user = new Customer();
        user.setId(UUID.randomUUID());
        user.setUsername("user");
        user.setEmail("EMAIL@TEST.COM");
        user.setName("Test User");
        
        when(authRestService.findAggregateByUsername("user")).thenReturn(user);
        when(authRestService.resolveRoles(user)).thenReturn(List.of("ROLE_CUSTOMER"));
        when(jwtUtils.generateJwtToken(any(), anyString(), anyString(), anyString(), anyList()))
            .thenReturn("new-token");
        when(jwtUtils.getExpirationFromJwtToken(anyString())).thenReturn(new Date(System.currentTimeMillis() + 60000));
        when(jwtUtils.getJwtExpirationMs()).thenReturn(3600000);
        
        LoginResponseDTO result = jwtTokenService.refreshToken("old-token", "user", "email@test.com");
        assertNotNull(result);
    }

    @Test
    void refreshToken_expirationNull_usesDefaultExpiry() {
        Customer user = new Customer();
        user.setId(UUID.randomUUID());
        user.setUsername("user");
        user.setEmail("email@test.com");
        user.setName("Test User");
        
        when(authRestService.findAggregateByUsername("user")).thenReturn(user);
        when(authRestService.resolveRoles(user)).thenReturn(List.of("ROLE_CUSTOMER"));
        when(jwtUtils.generateJwtToken(any(), anyString(), anyString(), anyString(), anyList()))
            .thenReturn("new-token");
        when(jwtUtils.getExpirationFromJwtToken(anyString())).thenReturn(null);
        when(jwtUtils.getJwtExpirationMs()).thenReturn(3600000);
        
        LoginResponseDTO result = jwtTokenService.refreshToken("old-token", "user", "email@test.com");
        assertNotNull(result);
        verify(jwtTokenBlacklist).revoke(eq("old-token"), anyLong());
    }

    @Test
    void refreshToken_blacklistThrowsException_stillReturnsToken() {
        Customer user = new Customer();
        user.setId(UUID.randomUUID());
        user.setUsername("user");
        user.setEmail("email@test.com");
        user.setName("Test User");
        
        when(authRestService.findAggregateByUsername("user")).thenReturn(user);
        when(authRestService.resolveRoles(user)).thenReturn(List.of("ROLE_CUSTOMER"));
        when(jwtUtils.generateJwtToken(any(), anyString(), anyString(), anyString(), anyList()))
            .thenReturn("new-token");
        when(jwtUtils.getExpirationFromJwtToken(anyString())).thenThrow(new RuntimeException("Error"));
        when(jwtUtils.getJwtExpirationMs()).thenReturn(3600000);
        
        LoginResponseDTO result = jwtTokenService.refreshToken("old-token", "user", "email@test.com");
        assertNotNull(result);
    }
}
