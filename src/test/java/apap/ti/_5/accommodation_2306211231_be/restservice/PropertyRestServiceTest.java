package apap.ti._5.accommodation_2306211231_be.restservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import apap.ti._5.accommodation_2306211231_be.models.Property;
import apap.ti._5.accommodation_2306211231_be.repository.PropertyRepository;

class PropertyRestServiceTest {

    PropertyRepository propertyRepository;
    PropertyRestService service;

    @BeforeEach
    void setup() {
        propertyRepository = mock(PropertyRepository.class);
        service = new PropertyRestService(propertyRepository, null, null, null, null);
    }

    @Test
    void count_delegatesToRepository() {
        when(propertyRepository.countByDeletedAtIsNull()).thenReturn(5L);
        assertEquals(5L, service.count());
        verify(propertyRepository, times(1)).countByDeletedAtIsNull();
    }

    @Test
    void getOwners_mapsDistinctOwners() {
        Property p = new Property();
        UUID u = UUID.randomUUID();
        p.setOwnerId(u);
        p.setOwnerName("Owner X");
        when(propertyRepository.findByDeletedAtIsNull()).thenReturn(List.of(p));

        var owners = service.getOwners();
        assertEquals(1, owners.size());
        assertEquals(u.toString(), owners.get(0).getOwnerId());
        assertEquals("Owner X", owners.get(0).getOwnerName());
    }
}
