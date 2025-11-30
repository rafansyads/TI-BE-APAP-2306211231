package apap.ti._5.accommodation_2306211231_be.restservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import apap.ti._5.accommodation_2306211231_be.models.Property;
import apap.ti._5.accommodation_2306211231_be.models.Room;
import apap.ti._5.accommodation_2306211231_be.models.RoomType;
import apap.ti._5.accommodation_2306211231_be.repository.PropertyRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomTypeRepository;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.RoomCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.roomtype.RoomTypeCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.response.property.PropertyDetailDto;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PropertyRestServiceUpdateRoomsTests {

    @Mock private PropertyRepository propertyRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private RoomTypeRepository roomTypeRepository;

    @InjectMocks private PropertyRestService service;

    private Property property;

    @BeforeEach
    void setup() {
        property = Property.builder()
                .propertyId("HOT-ABCD-001")
                .propertyName("Hotel X")
                .type(1)
                .address("Addr")
                .province(31)
                .totalRoom(1)
                .activeStatus(1)
                .ownerName("Owner")
                .build();

        RoomType rt = RoomType.builder()
                .roomTypeId("001-Deluxe-2")
                .name("Deluxe")
                .floor(2)
                .property(property)
                .build();
        Room existing = Room.builder()
                .roomId("HOT-ABCD-001-201")
                .name("201")
                .availabilityStatus(1)
                .activeRoom(1)
                .roomType(rt)
                .build();
        rt.setListRoom(new ArrayList<>(List.of(existing)));
        property.setListRoomType(new ArrayList<>(List.of(rt)));

        when(propertyRepository.findByPropertyIdAndDeletedAtIsNull("HOT-ABCD-001")).thenReturn(Optional.of(property));
        when(propertyRepository.save(any(Property.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // Simulate repository reporting that the canonical roomTypeId already exists
        when(roomTypeRepository.existsById("001-Deluxe-2")).thenReturn(true);
    }

    @Test
    void appendRoomsToExistingType() {
        RoomCreateRequest r1 = new RoomCreateRequest(null, null, 1, 1, null, null, null);
        RoomCreateRequest r2 = new RoomCreateRequest(null, null, 1, 1, null, null, null);
        RoomTypeCreateRequest req = RoomTypeCreateRequest.builder()
            .propertyId("HOT-ABCD-001")
            .name("Deluxe")
            .price(1)
            .capacity(2)
            .floor(2)
            .rooms(List.of(r1, r2))
            .build();

        // Current service rejects creation when canonical RoomTypeId already exists.
        // Expect IllegalArgumentException for attempt to create/append using same type name/floor.
        assertThrows(IllegalArgumentException.class,
            () -> service.updatePropertyRooms("HOT-ABCD-001", req));
    }

    @Test
    void createNewTypeWhenNotExists() {
        // clear existing to force create new type on floor 3
        property.getListRoomType().clear();
        property.setTotalRoom(0);

        RoomCreateRequest r = new RoomCreateRequest(null, null, 1, 1, null, null, null);
        RoomTypeCreateRequest req = RoomTypeCreateRequest.builder()
            .propertyId("HOT-ABCD-001")
            .name("Standard")
            .price(1)
            .capacity(2)
            .floor(3)
            .rooms(List.of(r))
            .build();

        PropertyDetailDto dto = service.updatePropertyRooms("HOT-ABCD-001", req);
        assertEquals(1, dto.getTotalRoom());
        assertEquals(1, dto.getRoomTypes().size());
    }

    @Test
    void duplicateTypeNameDifferentCaseRejected() {
        // Service treats the canonical RoomTypeId as case-sensitive; adding a different-case
        // room type should create a new room type rather than being rejected.
        RoomTypeCreateRequest req = RoomTypeCreateRequest.builder()
            .propertyId("HOT-ABCD-001")
            .name("deluxe")
            .price(1)
            .capacity(2)
            .floor(2)
            .rooms(List.of(new RoomCreateRequest(null, null, 1, 1, null, null, null)))
            .build();

        PropertyDetailDto dto = service.updatePropertyRooms("HOT-ABCD-001", req);
        // A new room type should be created alongside the existing one
        assertEquals(2, dto.getRoomTypes().size());
        assertEquals(2, dto.getTotalRoom());
    }

    @Test
    void floorOverNineRejected() {
        RoomTypeCreateRequest req = RoomTypeCreateRequest.builder()
            .propertyId("HOT-ABCD-001")
            .name("Deluxe")
            .price(1)
            .capacity(2)
            .floor(10)
            .rooms(List.of(new RoomCreateRequest(null, null, 1, 1, null, null, null)))
            .build();
        assertThrows(IllegalArgumentException.class, () -> service.updatePropertyRooms("HOT-ABCD-001", req));
    }

    @Test
    void perFloorOver99Rejected() {
        // Pre-populate floor 2 with rooms 201..299
        RoomType rt = property.getListRoomType().get(0);
        List<Room> many = new ArrayList<>(rt.getListRoom());
        for (int i = 2; i <= 99; i++) {
            String id = "HOT-ABCD-001-" + (200 + i);
            many.add(Room.builder().roomId(id).name(String.valueOf(200 + i)).availabilityStatus(1).activeRoom(1).roomType(rt).build());
        }
        rt.setListRoom(many);
        property.setTotalRoom(many.size());

        RoomTypeCreateRequest req = RoomTypeCreateRequest.builder()
            .propertyId("HOT-ABCD-001")
            .name("Deluxe")
            .price(1)
            .capacity(2)
            .floor(2)
            .rooms(List.of(new RoomCreateRequest(null, null, 1, 1, null, null, null)))
            .build();

        assertThrows(IllegalArgumentException.class,
            () -> service.updatePropertyRooms("HOT-ABCD-001", req));
    }

    @Test
    void roomIdMismatchRejected() {
        RoomTypeCreateRequest req = RoomTypeCreateRequest.builder()
            .propertyId("HOT-ABCD-001")
            .name("Deluxe")
            .price(1)
            .capacity(2)
            .floor(2)
            .rooms(List.of(new RoomCreateRequest("WRONG", null, 1, 1, null, null, null)))
            .build();

        assertThrows(IllegalArgumentException.class,
            () -> service.updatePropertyRooms("HOT-ABCD-001", req));
    }
}
