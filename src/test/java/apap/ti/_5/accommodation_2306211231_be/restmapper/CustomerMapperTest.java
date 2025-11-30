package apap.ti._5.accommodation_2306211231_be.restmapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import apap.ti._5.accommodation_2306211231_be.models.profile.Customer;
import apap.ti._5.accommodation_2306211231_be.restdto.response.profile.AccommodationCustomerResponseDTO;

class CustomerMapperTest {

    @Test
    void toDto_and_toCustomerResponseDto_mapsFields() {
        Customer c = new Customer();
        UUID id = UUID.randomUUID();
        c.setId(id);
        c.setUsername("jdoe");
        c.setName("John Doe");
        c.setEmail("jdoe@example.com");
        c.setSaldo(12345L);
        c.setCreatedAt(LocalDateTime.now());
        // ensure reviews list empty
        c.setReviews(java.util.List.of());

        AccommodationCustomerResponseDTO dto = CustomerMapper.toDTO(c);
        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals("jdoe", dto.getUsername());
        assertEquals("John Doe", dto.getName());
        assertEquals("jdoe@example.com", dto.getEmail());
        assertEquals(12345L, dto.getSaldo());

        var full = CustomerMapper.toCustomerResponseDto(c);
        assertNotNull(full);
        assertEquals(id.toString(), full.getId());
        assertEquals("jdoe", full.getUsername());
        assertEquals(java.util.List.of(), full.getReviewIds());
    }
}
