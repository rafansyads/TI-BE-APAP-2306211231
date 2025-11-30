package apap.ti._5.accommodation_2306211231_be.restcontroller;

import apap.ti._5.accommodation_2306211231_be.restdto.BaseRequestDto;
import apap.ti._5.accommodation_2306211231_be.restdto.request.auth.RegisterRequestDTO;
import apap.ti._5.accommodation_2306211231_be.models.profile.Customer;
import apap.ti._5.accommodation_2306211231_be.restservice.AuthRestService;
import apap.ti._5.accommodation_2306211231_be.security.jwt.JwtUtils;
import apap.ti._5.accommodation_2306211231_be.security.service.JwtTokenService;
import apap.ti._5.accommodation_2306211231_be.security.service.RefreshTokenService;
import apap.ti._5.accommodation_2306211231_be.security.service.JwtTokenBlacklist;
import org.junit.jupiter.api.Test;
 
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class AuthRestControllerUnitTest {

    @Test
    void register_missingData_returnsBadRequest() {
        var authService = Mockito.mock(AuthRestService.class);
        var controller = new apap.ti._5.accommodation_2306211231_be.restcontroller.AuthRestController(
                Mockito.mock(AuthenticationManager.class), Mockito.mock(JwtUtils.class), authService,
                Mockito.mock(JwtTokenService.class), Mockito.mock(RefreshTokenService.class),
                Mockito.mock(JwtTokenBlacklist.class), Mockito.mock(RestTemplate.class));

        BaseRequestDto<RegisterRequestDTO> req = new BaseRequestDto<>(); // data == null
        ResponseEntity<?> resp = controller.register(req);
        assertEquals(400, resp.getStatusCodeValue());
        assertNotNull(resp.getBody());
    }

    @Test
    void register_usernameTaken_returnsConflict() {
        var authService = Mockito.mock(AuthRestService.class);
        when(authService.existsUsername("taken")).thenReturn(true);

        var controller = new apap.ti._5.accommodation_2306211231_be.restcontroller.AuthRestController(
                Mockito.mock(AuthenticationManager.class), Mockito.mock(JwtUtils.class), authService,
                Mockito.mock(JwtTokenService.class), Mockito.mock(RefreshTokenService.class),
                Mockito.mock(JwtTokenBlacklist.class), Mockito.mock(RestTemplate.class));

        RegisterRequestDTO payload = new RegisterRequestDTO();
        payload.setUsername("taken");
        payload.setEmail("e@x.com");

        BaseRequestDto<RegisterRequestDTO> req = new BaseRequestDto<>();
        req.setData(payload);

        ResponseEntity<?> resp = controller.register(req);
        assertEquals(409, resp.getStatusCodeValue());
    }

    @Test
    void register_success_createsUser() {
        var authService = Mockito.mock(AuthRestService.class);
        when(authService.existsUsername("newuser")).thenReturn(false);
        when(authService.existsEmail("new@x.com")).thenReturn(false);
        Customer created = new Customer();
        created.setId(java.util.UUID.randomUUID());
        created.setUsername("newuser");
        created.setEmail("new@x.com");
        when(authService.register(Mockito.any())).thenReturn(created);
        // resolveRoles is used by the controller; ensure it returns at least one role
        when(authService.resolveRoles(Mockito.any())).thenReturn(java.util.List.of("END_USER"));

        var controller = new apap.ti._5.accommodation_2306211231_be.restcontroller.AuthRestController(
                Mockito.mock(AuthenticationManager.class), Mockito.mock(JwtUtils.class), authService,
                Mockito.mock(JwtTokenService.class), Mockito.mock(RefreshTokenService.class),
                Mockito.mock(JwtTokenBlacklist.class), Mockito.mock(RestTemplate.class));

        RegisterRequestDTO payload = new RegisterRequestDTO();
        payload.setUsername("newuser");
        payload.setEmail("new@x.com");
        payload.setPassword("p");
        BaseRequestDto<RegisterRequestDTO> req = new BaseRequestDto<>();
        req.setData(payload);

        ResponseEntity<?> resp = controller.register(req);
        assertEquals(201, resp.getStatusCodeValue());
        assertNotNull(resp.getBody());
    }
}
