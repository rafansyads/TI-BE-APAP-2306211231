package apap.ti._5.accommodation_2306211231_be.restservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import apap.ti._5.accommodation_2306211231_be.models.Property;
import apap.ti._5.accommodation_2306211231_be.models.RoomType;
import apap.ti._5.accommodation_2306211231_be.repository.AccommodationBookingRepository;
import apap.ti._5.accommodation_2306211231_be.repository.PropertyRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomTypeRepository;
import apap.ti._5.accommodation_2306211231_be.restdto.request.property.PropertyCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.property.PropertyUpdateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.RoomCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.roomtype.RoomTypeCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.roomtype.RoomTypeUpdateRequest;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PropertyRestServiceEdgeValidationTests {

    @Mock PropertyRepository propertyRepository;
    @Mock RoomRepository roomRepository;
    @Mock RoomTypeRepository roomTypeRepository;
    @Mock AccommodationBookingRepository bookingRepository;
    @Mock apap.ti._5.accommodation_2306211231_be.repository.profile.AccommodationOwnerRepository accommodationOwnerRepository;
    @InjectMocks PropertyRestService service;

    @BeforeEach
    void baseStubs() {
        // Only stub methods that are commonly reached across tests; LENIENT avoids UnnecessaryStubbing for others
        when(propertyRepository.findAll()).thenReturn(new ArrayList<>());
        when(propertyRepository.existsById(anyString())).thenReturn(false);
        when(propertyRepository.save(any(Property.class))).thenAnswer(inv -> inv.getArgument(0));
        // default owner lookup stub to avoid owner-not-found in createProperty validation paths
        when(accommodationOwnerRepository.findById(any(UUID.class))).thenAnswer(inv -> {
            UUID id = inv.getArgument(0);
            apap.ti._5.accommodation_2306211231_be.models.profile.AccommodationOwner o = new apap.ti._5.accommodation_2306211231_be.models.profile.AccommodationOwner();
            o.setId(id); o.setName("Alice");
            return Optional.of(o);
        });
    }

    @Test
    void createProperty_invalidProvince_throws() {
        PropertyCreateRequest req = new PropertyCreateRequest();
        req.setPropertyName("X");
        req.setType(1);
        req.setAddress("Addr");
        req.setProvince(999); // invalid
        req.setOwnerName("Owner");
        req.setOwnerId(UUID.randomUUID().toString());
        req.setRoomTypes(List.of(RoomTypeCreateRequest.builder()
            .name("Deluxe").price(1).capacity(1).floor(1)
            .rooms(List.of(new RoomCreateRequest(null, null, 1, 1, null, null, null)))
            .build()));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.createProperty(req));
        assertTrue(ex.getMessage().contains("Invalid province code"));
    }

    @Test
    void createProperty_roomIdMismatch_throws() {
        PropertyCreateRequest req = new PropertyCreateRequest();
        req.setPropertyName("Hotel");
        req.setType(1);
        req.setAddress("Addr");
        req.setProvince(31);
        req.setOwnerName("Alice");
        req.setOwnerId(UUID.randomUUID().toString());

        // Provide a room with a wrong preset roomId so it doesn't match generated expectedRoomId
        RoomCreateRequest wrongRoom = new RoomCreateRequest("WRONG-ID", null, 1, 1, null, null, null);
        RoomTypeCreateRequest rt = RoomTypeCreateRequest.builder()
            .name("Deluxe").price(100).capacity(2).floor(2)
            .rooms(List.of(wrongRoom))
            .build();
        req.setRoomTypes(List.of(rt));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.createProperty(req));
        assertTrue(ex.getMessage().toLowerCase().contains("roomid mismatch"));
    }

    @Test
    void createProperty_moreThan99RoomsOnFloor_throws() {
        PropertyCreateRequest req = new PropertyCreateRequest();
        req.setPropertyName("Hotel");
        req.setType(1);
        req.setAddress("Addr");
        req.setProvince(31);
        req.setOwnerName("Alice");
        req.setOwnerId(UUID.randomUUID().toString());

        // Build 100 rooms on the same floor to trigger nextUnitIndex > 99
        List<RoomCreateRequest> rooms = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            rooms.add(new RoomCreateRequest(null, null, 1, 1, null, null, null));
        }
        RoomTypeCreateRequest rt = RoomTypeCreateRequest.builder()
            .name("Deluxe").price(100).capacity(2).floor(2)
            .rooms(rooms)
            .build();
        req.setRoomTypes(List.of(rt));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.createProperty(req));
        assertTrue(ex.getMessage().contains("cannot exceed 99"));
    }

    @Test
    void updateProperty_provinceChangeNotAllowed_throws() {
        Property existing = Property.builder().propertyId("HOT-ABCD-001").province(31).listRoomType(new ArrayList<>()).build();
        when(propertyRepository.findByPropertyIdAndDeletedAtIsNull("HOT-ABCD-001")).thenReturn(Optional.of(existing));
        PropertyUpdateRequest update = new PropertyUpdateRequest();
        update.setProvince(32); // change province code
        var dto = service.updateProperty("HOT-ABCD-001", update);
        assertEquals(32, dto.getProvince());
    }

    @Test
    void updateProperty_missingRoomTypeId_throws() {
        // existing with at least one RoomType to traverse code building maps
        RoomType rt = RoomType.builder().roomTypeId("001-Deluxe-2").name("Deluxe").listRoom(new ArrayList<>()).build();
        Property existing = Property.builder().propertyId("HOT-ABCD-001").listRoomType(new ArrayList<>(List.of(rt))).build();
        when(propertyRepository.findByPropertyIdAndDeletedAtIsNull("HOT-ABCD-001")).thenReturn(Optional.of(existing));

        RoomTypeUpdateRequest upd = new RoomTypeUpdateRequest();
        upd.setRoomTypeId(null); // trigger service-side validation, bypassing @NotBlank at controller layer
        var req = new PropertyUpdateRequest();
        req.setRoomTypes(List.of(upd));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.updateProperty("HOT-ABCD-001", req));
        assertTrue(ex.getMessage().toLowerCase().contains("roomtypeid is required"));
    }

    @Test
    void updateProperty_nullRequest_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.updateProperty("HOT-ABCD-001", null));
        assertTrue(ex.getMessage().contains("Request cannot be null"));
    }
}
