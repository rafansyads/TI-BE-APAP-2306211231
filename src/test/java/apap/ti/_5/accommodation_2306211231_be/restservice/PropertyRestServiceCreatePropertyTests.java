package apap.ti._5.accommodation_2306211231_be.restservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import apap.ti._5.accommodation_2306211231_be.models.Property;
import apap.ti._5.accommodation_2306211231_be.repository.PropertyRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomTypeRepository;
import apap.ti._5.accommodation_2306211231_be.restdto.request.property.PropertyCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.RoomCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.roomtype.RoomTypeCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.response.property.PropertyDetailDto;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PropertyRestServiceCreatePropertyTests {

    @Mock private PropertyRepository propertyRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private RoomTypeRepository roomTypeRepository;
    @Mock private apap.ti._5.accommodation_2306211231_be.repository.profile.AccommodationOwnerRepository accommodationOwnerRepository;

    @InjectMocks private PropertyRestService service;

    @BeforeEach
    void setup() {
        when(propertyRepository.findAll()).thenReturn(new ArrayList<>()); // max counter = 0
        when(propertyRepository.existsById(anyString())).thenReturn(false);
        when(propertyRepository.findFirstByOwnerId(any(UUID.class))).thenReturn(Optional.empty());
        when(propertyRepository.save(any(Property.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(accommodationOwnerRepository.findById(any(java.util.UUID.class))).thenAnswer(inv -> {
            java.util.UUID id = inv.getArgument(0);
            apap.ti._5.accommodation_2306211231_be.models.profile.AccommodationOwner o = new apap.ti._5.accommodation_2306211231_be.models.profile.AccommodationOwner();
            o.setId(id); o.setName("ownerName");
            return java.util.Optional.of(o);
        });
    }

    @Test
    void createPropertyWithOneRoomTypeAndTwoRooms() {
        PropertyCreateRequest req = new PropertyCreateRequest();
        req.setPropertyName("My Hotel");
        req.setType(1);
        req.setAddress("Jl. Sudirman");
        req.setProvince(31);
        req.setDescription("Desc");
        req.setTotalRoom(0); // will be overridden
        req.setActiveStatus(1);
        req.setOwnerName("Alice");
        req.setOwnerId(UUID.randomUUID().toString());

        RoomCreateRequest r1 = new RoomCreateRequest(null, null, 1, 1, null, null, null);
        RoomCreateRequest r2 = new RoomCreateRequest(null, null, 1, 1, null, null, null);

        RoomTypeCreateRequest rt = RoomTypeCreateRequest.builder()
            .name("Deluxe")
            .price(100000)
            .description("Nice")
            .capacity(2)
            .facility("AC")
            .floor(2)
            .rooms(List.of(r1, r2))
            .build();

        req.setRoomTypes(List.of(rt));

        // stub owner lookup to match provided ownerName
        UUID oid = UUID.fromString(req.getOwnerId());
        when(accommodationOwnerRepository.findById(eq(oid))).thenAnswer(inv -> {
            apap.ti._5.accommodation_2306211231_be.models.profile.AccommodationOwner o = new apap.ti._5.accommodation_2306211231_be.models.profile.AccommodationOwner();
            o.setId(oid); o.setName("Alice");
            return Optional.of(o);
        });

        PropertyDetailDto dto = service.createProperty(req);
        assertNotNull(dto);
        assertNotNull(dto.getPropertyId());
        assertEquals(2, dto.getTotalRoom());
        assertEquals(1, dto.getRoomTypes().size());
        assertEquals(2, dto.getRooms().size());
        // Room names auto-generated from numeric part
        assertTrue(dto.getRooms().get(0).getName().matches("\\d+"));
        assertTrue(dto.getRooms().get(1).getName().matches("\\d+"));
    }

    @Test
    void duplicateRoomTypeOnSameFloorIsRejected() {
        PropertyCreateRequest req = new PropertyCreateRequest();
        req.setPropertyName("My Apt");
        req.setType(3);
        req.setAddress("Jl. Gatot");
        req.setProvince(31);
        req.setDescription("Desc");
        req.setTotalRoom(0);
        req.setActiveStatus(1);
        req.setOwnerName("Bob");
        req.setOwnerId(UUID.randomUUID().toString());

        RoomTypeCreateRequest rt1 = RoomTypeCreateRequest.builder()
            .name("Studio").price(1).capacity(1).floor(1)
            .rooms(List.of(new RoomCreateRequest(null, null, 1, 1, null, null, null)))
            .build();
        RoomTypeCreateRequest rt2 = RoomTypeCreateRequest.builder()
            .name("Studio").price(1).capacity(1).floor(1)
            .rooms(List.of(new RoomCreateRequest(null, null, 1, 1, null, null, null)))
            .build();
        req.setRoomTypes(List.of(rt1, rt2));

        // stub owner lookup to match provided ownerName (avoid owner-not-found/mismatch)
        UUID oid2 = UUID.fromString(req.getOwnerId());
        when(accommodationOwnerRepository.findById(eq(oid2))).thenAnswer(inv -> {
            apap.ti._5.accommodation_2306211231_be.models.profile.AccommodationOwner o = new apap.ti._5.accommodation_2306211231_be.models.profile.AccommodationOwner();
            o.setId(oid2); o.setName("Bob");
            return Optional.of(o);
        });

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.createProperty(req));
        assertTrue(ex.getMessage().toLowerCase().contains("duplicate room type"));
    }
}
