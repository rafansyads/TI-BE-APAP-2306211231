package apap.ti._5.accommodation_2306211231_be.restmapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import apap.ti._5.accommodation_2306211231_be.models.profile.Customer;
import apap.ti._5.accommodation_2306211231_be.restdto.response.profile.EndUserResponseDTO;

class EndUserMapperTest {

    @Test
    void toDTO_mapsCommonFields_and_saldoForCustomer() {
        Customer u = new Customer();
        UUID id = UUID.randomUUID();
        u.setId(id);
        u.setUsername("bob");
        u.setName("Bob");
        u.setEmail("bob@example.com");
        u.setCreatedAt(LocalDateTime.now());

        EndUserResponseDTO dto = EndUserMapper.toDTO(u, "ROLE_USER");
        assertNotNull(dto);
        assertEquals(id.toString(), dto.getId());
        assertEquals("bob", dto.getUsername());
        assertEquals("ROLE_USER", dto.getRole());
    }
}
