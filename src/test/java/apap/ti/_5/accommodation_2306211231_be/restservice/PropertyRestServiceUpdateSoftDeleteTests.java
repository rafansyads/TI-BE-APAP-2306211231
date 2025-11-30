package apap.ti._5.accommodation_2306211231_be.restservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
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

import apap.ti._5.accommodation_2306211231_be.models.Property;
import apap.ti._5.accommodation_2306211231_be.models.Room;
import apap.ti._5.accommodation_2306211231_be.models.RoomType;
import apap.ti._5.accommodation_2306211231_be.repository.AccommodationBookingRepository;
import apap.ti._5.accommodation_2306211231_be.repository.PropertyRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomTypeRepository;
import apap.ti._5.accommodation_2306211231_be.restdto.request.property.PropertyUpdateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.roomtype.RoomTypeCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.roomtype.RoomTypeUpdateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.RoomCreateRequest;

@ExtendWith(MockitoExtension.class)
class PropertyRestServiceUpdateSoftDeleteTests {

    @Mock PropertyRepository propertyRepository;
    @Mock RoomRepository roomRepository;
    @Mock RoomTypeRepository roomTypeRepository;
    @Mock AccommodationBookingRepository bookingRepository;
    @Mock apap.ti._5.accommodation_2306211231_be.repository.profile.AccommodationOwnerRepository accommodationOwnerRepository;
    @InjectMocks PropertyRestService service;

    private Property property;

    @BeforeEach
    void setup() {
        property = Property.builder()
            .propertyId("HOT-ABCD-001")
            .propertyName("Hotel Z")
            .type(1)
            .province(31)
            .address("Addr")
            .description("Desc")
            .ownerId(UUID.randomUUID())
            .ownerName("Alice")
            .totalRoom(1)
            .activeStatus(1)
            .build();
        RoomType rt = RoomType.builder().roomTypeId("001-Deluxe-2").name("Deluxe").price(100).capacity(2).floor(2).property(property).build();
        Room room = Room.builder().roomId("HOT-ABCD-001-201").name("201").availabilityStatus(1).activeRoom(1).roomType(rt).build();
        rt.setListRoom(new ArrayList<>(List.of(room)));
        property.setListRoomType(new ArrayList<>(List.of(rt)));
    }

    @Test
    void updateProperty_success_updatesMutableFieldsOnly() {
        when(propertyRepository.findByPropertyIdAndDeletedAtIsNull("HOT-ABCD-001")).thenReturn(Optional.of(property));
        // stub owner lookup to match property owner for update flows
        when(accommodationOwnerRepository.findById(any(UUID.class))).thenAnswer(inv -> {
            UUID id = inv.getArgument(0);
            apap.ti._5.accommodation_2306211231_be.models.profile.AccommodationOwner o = new apap.ti._5.accommodation_2306211231_be.models.profile.AccommodationOwner();
            o.setId(id); o.setName(property.getOwnerName());
            return Optional.of(o);
        });
        when(propertyRepository.save(any(Property.class))).thenAnswer(inv -> inv.getArgument(0));

        PropertyUpdateRequest req = new PropertyUpdateRequest();
        req.setPropertyId("HOT-ABCD-001");
        req.setPropertyName("Hotel Z Updated");
        req.setAddress("New Address");
        req.setDescription("New Desc");
        req.setOwnerName(property.getOwnerName());
        req.setOwnerId(property.getOwnerId().toString());
        // keep province same (service disallows changing existing province)
        req.setProvince(property.getProvince());

        var dto = service.updateProperty("HOT-ABCD-001", req);
        assertEquals("Hotel Z Updated", dto.getPropertyName());
        assertEquals("New Address", dto.getAddress());
        assertEquals("New Desc", dto.getDescription());
        // province should remain original (31) because change not allowed once set
        assertEquals(31, dto.getProvince());
    }

    @Test
    void updateProperty_roomTypeNestedUpdates_priceChange() {
        when(propertyRepository.findByPropertyIdAndDeletedAtIsNull("HOT-ABCD-001")).thenReturn(Optional.of(property));
        when(propertyRepository.save(any(Property.class))).thenAnswer(inv -> inv.getArgument(0));

        // Stub owner lookup to avoid Owner not found errors
        when(accommodationOwnerRepository.findById(any(UUID.class))).thenAnswer(inv -> {
            UUID id = inv.getArgument(0);
            apap.ti._5.accommodation_2306211231_be.models.profile.AccommodationOwner o = new apap.ti._5.accommodation_2306211231_be.models.profile.AccommodationOwner();
            o.setId(id); o.setName(property.getOwnerName());
            return Optional.of(o);
        });

        RoomTypeUpdateRequest upd = new RoomTypeUpdateRequest(property.getListRoomType().get(0).getRoomTypeId(), null, 150, null, 2, null, 2);
        PropertyUpdateRequest req = new PropertyUpdateRequest();
        req.setPropertyId("HOT-ABCD-001");
        req.setPropertyName(property.getPropertyName());
        req.setAddress(property.getAddress());
        req.setDescription(property.getDescription());
        req.setOwnerName(property.getOwnerName());
        req.setOwnerId(property.getOwnerId().toString());
        req.setProvince(property.getProvince());
        req.setRoomTypes(List.of(upd));

        var dto = service.updateProperty("HOT-ABCD-001", req);
        assertTrue(dto.getRoomTypes().stream().anyMatch(t -> t.getPrice()==150));
    }

    @Test
    void softDelete_marksDeletedAt() {
        when(propertyRepository.findByPropertyIdAndDeletedAtIsNull("HOT-ABCD-001")).thenReturn(Optional.of(property));
    when(propertyRepository.save(any(Property.class))).thenAnswer(inv -> inv.getArgument(0));
        assertNull(property.getDeletedAt());
        service.softDeleteProperty("HOT-ABCD-001");
        assertNotNull(property.getDeletedAt());
        assertTrue(property.getDeletedAt().isBefore(LocalDateTime.now().plusSeconds(2)));
    }

    @Test
    void softDelete_missing_throws() {
        when(propertyRepository.findByPropertyIdAndDeletedAtIsNull("MISSING")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.softDeleteProperty("MISSING"));
    }
}
