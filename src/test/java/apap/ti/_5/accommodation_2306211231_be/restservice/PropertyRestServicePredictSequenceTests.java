package apap.ti._5.accommodation_2306211231_be.restservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import apap.ti._5.accommodation_2306211231_be.models.Property;
import apap.ti._5.accommodation_2306211231_be.repository.PropertyRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomTypeRepository;

@ExtendWith(MockitoExtension.class)
class PropertyRestServicePredictSequenceTests {

    @Mock private PropertyRepository propertyRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private RoomTypeRepository roomTypeRepository;
    @InjectMocks private PropertyRestService service;

    @Test
    void predictNextPropertySequence_incrementsMaxSuffix() {
    Property p1 = Property.builder().propertyId("HOT-ABCD-001").build();
    Property p2 = Property.builder().propertyId("HOT-ABCD-007").build();
        when(propertyRepository.findAll()).thenReturn(List.of(p1, p2));
        int next = service.predictNextPropertySequence();
        assertEquals(8, next); // max suffix 7 -> next 8
    }
}
