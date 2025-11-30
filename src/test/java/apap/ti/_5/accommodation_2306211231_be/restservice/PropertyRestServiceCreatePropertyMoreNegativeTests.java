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
import apap.ti._5.accommodation_2306211231_be.repository.AccommodationBookingRepository;
import apap.ti._5.accommodation_2306211231_be.repository.PropertyRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomTypeRepository;
import apap.ti._5.accommodation_2306211231_be.restdto.request.property.PropertyCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.RoomCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.roomtype.RoomTypeCreateRequest;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PropertyRestServiceCreatePropertyMoreNegativeTests {

    @Mock PropertyRepository propertyRepository;
    @Mock RoomRepository roomRepository;
    @Mock RoomTypeRepository roomTypeRepository;
    @Mock AccommodationBookingRepository bookingRepository;
    @Mock apap.ti._5.accommodation_2306211231_be.repository.profile.AccommodationOwnerRepository accommodationOwnerRepository;
    @InjectMocks PropertyRestService service;

    @BeforeEach
    void baseStubs() {
        when(propertyRepository.findAll()).thenReturn(new ArrayList<>());
        when(propertyRepository.existsById(anyString())).thenReturn(false);
        when(propertyRepository.save(any(Property.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private PropertyCreateRequest minimalCreateReq(UUID ownerId) {
        RoomCreateRequest roomReq = new RoomCreateRequest(null, null, 1, 1, null, null, null);
        RoomTypeCreateRequest rtReq = RoomTypeCreateRequest.builder()
                .name("Deluxe").price(100000).capacity(2).floor(2)
                .rooms(List.of(roomReq))
                .build();
        PropertyCreateRequest req = new PropertyCreateRequest();
        req.setPropertyName("Hotel X");
        req.setType(1);
        req.setAddress("Addr");
        req.setProvince(31);
        req.setDescription("Desc");
        req.setOwnerId(ownerId.toString());
        req.setOwnerName("Owner");
        req.setRoomTypes(List.of(rtReq));
        return req;
    }

    @Test
    void createProperty_roomTypeIdMismatch_rejected() {
        UUID owner = UUID.randomUUID();
        // no prior owner entry
        when(propertyRepository.findFirstByOwnerId(owner)).thenReturn(Optional.empty());

        // stub owner lookup to exist with matching name
        when(accommodationOwnerRepository.findById(eq(owner))).thenAnswer(inv -> {
            apap.ti._5.accommodation_2306211231_be.models.profile.AccommodationOwner o = new apap.ti._5.accommodation_2306211231_be.models.profile.AccommodationOwner();
            o.setId(owner); o.setName("Owner");
            return Optional.of(o);
        });

        // Build request but force a wrong roomTypeId from client
        var req = minimalCreateReq(owner);
    var rtList = req.getRoomTypes();
    var orig = rtList.get(0);
    var wrong = RoomTypeCreateRequest.builder()
        .name(orig.getName())
        .description(orig.getDescription())
        .price(orig.getPrice())
        .capacity(orig.getCapacity())
        .facility(orig.getFacility())
        .floor(orig.getFloor())
        .rooms(orig.getRooms())
        .roomTypeId("WRONG-ID-123")
        .build();
    req.setRoomTypes(List.of(wrong));

        var ex = assertThrows(IllegalArgumentException.class, () -> service.createProperty(req));
        assertTrue(ex.getMessage().toLowerCase().contains("roomtypeid mismatch"));
    }

    @Test
    void createProperty_ownerUuidNameMismatch_rejected() {
        UUID owner = UUID.randomUUID();
        // Simulate existing property for the same owner UUID but different ownerName
        var existing = Property.builder().ownerId(owner).ownerName("Another Owner").propertyId("HOT-XXXX-001").build();
        when(propertyRepository.findFirstByOwnerId(owner)).thenReturn(Optional.of(existing));

        var req = minimalCreateReq(owner);
        req.setOwnerName("Owner"); // different from existing

        // stub owner lookup to return owner record matching existing's name
        when(accommodationOwnerRepository.findById(eq(owner))).thenAnswer(inv -> {
            apap.ti._5.accommodation_2306211231_be.models.profile.AccommodationOwner o = new apap.ti._5.accommodation_2306211231_be.models.profile.AccommodationOwner();
            o.setId(owner); o.setName("Another Owner");
            return Optional.of(o);
        });

        var ex = assertThrows(IllegalArgumentException.class, () -> service.createProperty(req));
        assertTrue(ex.getMessage().toLowerCase().contains("owner uuid/name mismatch"));
    }
}
