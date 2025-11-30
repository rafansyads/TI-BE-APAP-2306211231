package apap.ti._5.accommodation_2306211231_be.restservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import apap.ti._5.accommodation_2306211231_be.models.Property;
import apap.ti._5.accommodation_2306211231_be.repository.AccommodationBookingRepository;
import apap.ti._5.accommodation_2306211231_be.repository.PropertyRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomTypeRepository;
import apap.ti._5.accommodation_2306211231_be.restdto.request.property.PropertyCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.RoomCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.roomtype.RoomTypeCreateRequest;
import apap.ti._5.accommodation_2306211231_be.util.IdUtil;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PropertyRestServiceAllocatePropertyIdTests {

    @Mock PropertyRepository propertyRepository;
    @Mock RoomRepository roomRepository;
    @Mock RoomTypeRepository roomTypeRepository;
    @Mock AccommodationBookingRepository bookingRepository;
    @Mock apap.ti._5.accommodation_2306211231_be.repository.profile.AccommodationOwnerRepository accommodationOwnerRepository;
    @InjectMocks PropertyRestService service;

    private PropertyCreateRequest buildMinimalCreateRequest(UUID ownerId) {
        // RoomCreateRequest has no builder; use all-args constructor
        RoomCreateRequest roomReq = new RoomCreateRequest(
            null, // roomId auto-generated
            null, // name auto-generated from number
            1,    // availabilityStatus
            1,    // activeRoom
            null, // maintenanceStart
            null, // maintenanceEnd
            null  // roomTypeId (single type scenario)
        );
        RoomTypeCreateRequest rtReq = RoomTypeCreateRequest.builder()
            .name("Deluxe")
            .price(100)
            .capacity(2)
            .floor(2)
            .rooms(List.of(roomReq))
            .build();
        PropertyCreateRequest req = new PropertyCreateRequest();
        req.setPropertyName("Hotel A");
        req.setType(1); // HOT
        req.setAddress("Addr");
        req.setDescription("Desc");
        req.setProvince(31);
        req.setOwnerId(ownerId.toString());
        req.setOwnerName("Owner");
        req.setRoomTypes(List.of(rtReq));
        return req;
    }

    @Test
    void createProperty_allocatesNextCounter_withRetry() {
        // existing max = 5 -> try 6 (collide), then 7 (ok)
        UUID ownerId = UUID.randomUUID();
        when(propertyRepository.findAll()).thenReturn(List.of(
            Property.builder().propertyId("HOT-ABCD-005").build()
        ));
        // First candidate (006) exists -> retry; 007 not exists
        String cand6 = IdUtil.generatePropertyId(1, ownerId, 6);
        String cand7 = IdUtil.generatePropertyId(1, ownerId, 7);
        when(propertyRepository.existsById(any(String.class))).thenAnswer(inv -> {
            String pid = inv.getArgument(0);
            if (pid.equals(cand6)) return true;
            if (pid.equals(cand7)) return false;
            return false;
        });
        when(propertyRepository.findFirstByOwnerId(ownerId)).thenReturn(Optional.empty());
        when(propertyRepository.save(any(Property.class))).thenAnswer(inv -> inv.getArgument(0));

        // stub owner lookup used by createProperty
        when(accommodationOwnerRepository.findById(eq(ownerId))).thenAnswer(inv -> {
            apap.ti._5.accommodation_2306211231_be.models.profile.AccommodationOwner o = new apap.ti._5.accommodation_2306211231_be.models.profile.AccommodationOwner();
            o.setId(ownerId);
            o.setName("Owner");
            return Optional.of(o);
        });

        var dto = service.createProperty(buildMinimalCreateRequest(ownerId));
        assertNotNull(dto);
        assertEquals(cand7, dto.getPropertyId());
    }

    @Test
    void createProperty_fallbackCounter_whenAllRetriesCollide() {
        UUID ownerId = UUID.randomUUID();
        when(propertyRepository.findAll()).thenReturn(List.of(
            Property.builder().propertyId("HOT-ABCD-010").build()
        ));
        // Force collisions for attempts 11..15
        when(propertyRepository.existsById(any(String.class))).thenReturn(true);
        when(propertyRepository.findFirstByOwnerId(ownerId)).thenReturn(Optional.empty());
        when(propertyRepository.save(any(Property.class))).thenAnswer(inv -> inv.getArgument(0));

        // stub owner lookup used by createProperty
        when(accommodationOwnerRepository.findById(eq(ownerId))).thenAnswer(inv -> {
            apap.ti._5.accommodation_2306211231_be.models.profile.AccommodationOwner o = new apap.ti._5.accommodation_2306211231_be.models.profile.AccommodationOwner();
            o.setId(ownerId);
            o.setName("Owner");
            return Optional.of(o);
        });

        var dto = service.createProperty(buildMinimalCreateRequest(ownerId));
        assertNotNull(dto);
        // Numeric suffix should be in [max+100, max+999]
        int suffix = IdUtil.extractPropertyCounter(dto.getPropertyId());
        assertTrue(suffix >= 110 && suffix <= 1009); // max=10 => 10+100 .. 10+100+899
    }
}
