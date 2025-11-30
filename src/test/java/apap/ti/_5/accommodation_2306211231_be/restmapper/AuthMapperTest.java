package apap.ti._5.accommodation_2306211231_be.restmapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import apap.ti._5.accommodation_2306211231_be.models.profile.Customer;
import apap.ti._5.accommodation_2306211231_be.restdto.response.auth.LoginResponseDTO;
import apap.ti._5.accommodation_2306211231_be.restdto.response.auth.RegisterResponseDTO;

class AuthMapperTest {

    @Test
    void toLoginResponseDto_and_register_mapsFields() {
        Customer u = new Customer();
        UUID id = UUID.randomUUID();
        u.setId(id);
        u.setUsername("alice");
        u.setName("Alice");
        u.setEmail("alice@example.com");

        LoginResponseDTO login = AuthMapper.toLoginResponseDto(u, List.of("CUSTOMER"), "token-123", Instant.now().plusSeconds(60));
        assertNotNull(login);
        assertEquals(id.toString(), login.getId());
        assertEquals("alice", login.getUsername());
        assertEquals("token-123", login.getToken());

        RegisterResponseDTO reg = AuthMapper.toRegisterResponseDto(u, "CUSTOMER", Instant.now());
        assertNotNull(reg);
        assertEquals(id.toString(), reg.getId());
        assertEquals("alice", reg.getUsername());
    }
}
