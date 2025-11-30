package apap.ti._5.accommodation_2306211231_be.restservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import apap.ti._5.accommodation_2306211231_be.models.AccommodationBooking;
import apap.ti._5.accommodation_2306211231_be.models.Property;
import apap.ti._5.accommodation_2306211231_be.models.Room;
import apap.ti._5.accommodation_2306211231_be.models.RoomType;
import apap.ti._5.accommodation_2306211231_be.repository.AccommodationBookingRepository;
import apap.ti._5.accommodation_2306211231_be.repository.PropertyRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomTypeRepository;
import apap.ti._5.accommodation_2306211231_be.restdto.request.property.PropertyUpdateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.roomtype.RoomTypeUpdateRequest;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PropertyRestServiceUpdatePriceDeltaTests {

    @Mock PropertyRepository propertyRepository;
    @Mock RoomRepository roomRepository;
    @Mock RoomTypeRepository roomTypeRepository;
    @Mock AccommodationBookingRepository bookingRepository;
    @Mock apap.ti._5.accommodation_2306211231_be.repository.profile.AccommodationOwnerRepository accommodationOwnerRepository;
    @InjectMocks PropertyRestService service;

    private Property property;
    private RoomType roomType;
    private Room room;

    @BeforeEach
    void setup() {
        property = Property.builder()
            .propertyId("HOT-ABCD-001")
            .propertyName("Hotel Z")
            .province(31)
            .ownerId(UUID.randomUUID())
            .ownerName("Alice")
            .listRoomType(new ArrayList<>())
            .build();
        roomType = RoomType.builder()
            .roomTypeId("001-Deluxe-2")
            .name("Deluxe")
            .price(100)
            .capacity(2)
            .floor(2)
            .property(property)
            .listRoom(new ArrayList<>())
            .build();
        room = Room.builder()
            .roomId("HOT-ABCD-001-201")
            .name("201")
            .availabilityStatus(1)
            .activeRoom(1)
            .roomType(roomType)
            .build();
        roomType.getListRoom().add(room);
        property.getListRoomType().add(roomType);
        when(propertyRepository.findByPropertyIdAndDeletedAtIsNull("HOT-ABCD-001")).thenReturn(Optional.of(property));
        when(propertyRepository.save(any(Property.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookingRepository.save(any(AccommodationBooking.class))).thenAnswer(inv -> inv.getArgument(0));

        // Stub owner lookup used in updateProperty validation
        apap.ti._5.accommodation_2306211231_be.models.profile.AccommodationOwner ownerProfile =
            new apap.ti._5.accommodation_2306211231_be.models.profile.AccommodationOwner();
        ownerProfile.setId(property.getOwnerId());
        ownerProfile.setName(property.getOwnerName());
        when(accommodationOwnerRepository.findById(property.getOwnerId())).thenReturn(Optional.of(ownerProfile));
    }

    @Test
    void updateProperty_priceIncrease_setsExtraPay_forWaiting() {
        // booking status 0 (waiting), 2 days -> delta 50 per night => extraPay 100
        AccommodationBooking b = new AccommodationBooking();
        b.setBookingId("B-1");
        b.setStatus(0);
        b.setCheckInDate(LocalDateTime.now().plusDays(10).withHour(14));
        b.setCheckOutDate(LocalDateTime.now().plusDays(12).withHour(12));
        b.setTotalDays(2);
        b.setTotalPrice(200); // baseline (2 * 100)
        b.setCustomerId(UUID.randomUUID());
        b.setCustomerName("Bob");
        b.setCustomerEmail("bob@example.com");
        b.setCustomerPhone("08123456789");
        b.setIsBreakfast(false);
        b.setRefund(0);
        b.setExtraPay(0);
        b.setCapacity(2);
        b.setRoom(room);
        room.setBookings(List.of(b));

    RoomTypeUpdateRequest rtUpd = new RoomTypeUpdateRequest(roomType.getRoomTypeId(), null, 150, null, roomType.getCapacity(), null, roomType.getFloor());
        PropertyUpdateRequest req = new PropertyUpdateRequest();
        req.setPropertyId(property.getPropertyId());
        req.setPropertyName(property.getPropertyName());
        req.setOwnerId(property.getOwnerId().toString());
        req.setOwnerName(property.getOwnerName());
        req.setProvince(property.getProvince());
        req.setRoomTypes(List.of(rtUpd));

        var dto = service.updateProperty("HOT-ABCD-001", req);
        assertNotNull(dto);
        // Verify booking adjustments applied
        assertEquals(100, b.getExtraPay());
        assertEquals(0, b.getRefund());
        assertEquals(0, b.getStatus());
        // totalPrice remains unchanged here
        assertEquals(200, b.getTotalPrice());
    }

    @Test
    void updateProperty_priceDecrease_setsRefund_andStatus3_forPaid() {
        // booking status 1 (paid), 3 days -> delta -20 per night => refund 60, status 3
        AccommodationBooking b = new AccommodationBooking();
        b.setBookingId("B-2");
        b.setStatus(1);
        b.setCheckInDate(LocalDateTime.now().plusDays(20).withHour(14));
        b.setCheckOutDate(LocalDateTime.now().plusDays(23).withHour(12));
        b.setTotalDays(3);
        b.setTotalPrice(300); // baseline (3 * 100)
        b.setCustomerId(UUID.randomUUID());
        b.setCustomerName("Eve");
        b.setCustomerEmail("eve@example.com");
        b.setCustomerPhone("08123456780");
        b.setIsBreakfast(false);
        b.setRefund(0);
        b.setExtraPay(0);
        b.setCapacity(2);
        b.setRoom(room);
        room.setBookings(List.of(b));

    RoomTypeUpdateRequest rtUpd = new RoomTypeUpdateRequest(roomType.getRoomTypeId(), null, 80, null, roomType.getCapacity(), null, roomType.getFloor());
        PropertyUpdateRequest req = new PropertyUpdateRequest();
        req.setPropertyId(property.getPropertyId());
        req.setPropertyName(property.getPropertyName());
        req.setOwnerId(property.getOwnerId().toString());
        req.setOwnerName(property.getOwnerName());
        req.setProvince(property.getProvince());
        req.setRoomTypes(List.of(rtUpd));

        var dto = service.updateProperty("HOT-ABCD-001", req);
        assertNotNull(dto);
        assertEquals(0, b.getExtraPay());
        assertEquals(60, b.getRefund());
        assertEquals(3, b.getStatus()); // refund requested
        assertEquals(300, b.getTotalPrice()); // unchanged until refund processed
    }

    @Test
    void updateProperty_capacityValidation_rejectsBelowOne() {
    RoomTypeUpdateRequest rtUpd = new RoomTypeUpdateRequest(roomType.getRoomTypeId(), null, roomType.getPrice(), null, 0, null, roomType.getFloor());
        PropertyUpdateRequest req = new PropertyUpdateRequest();
        req.setPropertyId(property.getPropertyId());
        req.setPropertyName(property.getPropertyName());
        req.setOwnerId(property.getOwnerId().toString());
        req.setOwnerName(property.getOwnerName());
        req.setProvince(property.getProvince());
        req.setRoomTypes(List.of(rtUpd));
        assertThrows(IllegalArgumentException.class, () -> service.updateProperty("HOT-ABCD-001", req));
    }

    @Test
    void updateProperty_capacityUpdate_success() {
    RoomTypeUpdateRequest rtUpd = new RoomTypeUpdateRequest(roomType.getRoomTypeId(), null, roomType.getPrice(), null, 3, null, roomType.getFloor());
        PropertyUpdateRequest req = new PropertyUpdateRequest();
        req.setPropertyId(property.getPropertyId());
        req.setPropertyName(property.getPropertyName());
        req.setOwnerId(property.getOwnerId().toString());
        req.setOwnerName(property.getOwnerName());
        req.setProvince(property.getProvince());
        req.setRoomTypes(List.of(rtUpd));
        var dto = service.updateProperty("HOT-ABCD-001", req);
        assertNotNull(dto);
        assertEquals(3, roomType.getCapacity());
    }

    @Test
    void updateProperty_zeroDelta_noExtraNoRefund_noStatusChange() {
        // booking paid with baseline 2 days * 100 = 200; keep price the same -> delta=0
        AccommodationBooking b = new AccommodationBooking();
        b.setBookingId("B-ZERO");
        b.setStatus(1);
        b.setCheckInDate(LocalDateTime.now().plusDays(5).withHour(14));
        b.setCheckOutDate(LocalDateTime.now().plusDays(7).withHour(12));
        b.setTotalDays(2);
        b.setTotalPrice(200);
        b.setCustomerId(UUID.randomUUID());
        b.setRoom(room);
        room.setBookings(List.of(b));

        // No price change
        RoomTypeUpdateRequest rtUpd = new RoomTypeUpdateRequest(roomType.getRoomTypeId(), null, roomType.getPrice(), null, roomType.getCapacity(), null, roomType.getFloor());
        PropertyUpdateRequest req = new PropertyUpdateRequest();
        req.setPropertyId(property.getPropertyId());
        req.setPropertyName(property.getPropertyName());
        req.setOwnerId(property.getOwnerId().toString());
        req.setOwnerName(property.getOwnerName());
        req.setProvince(property.getProvince());
        req.setRoomTypes(List.of(rtUpd));

        var dto = service.updateProperty("HOT-ABCD-001", req);
        assertNotNull(dto);
        assertEquals(1, b.getStatus());
    assertTrue(b.getExtraPay() == null || b.getExtraPay() == 0);
    assertTrue(b.getRefund() == null || b.getRefund() == 0);
        assertEquals(200, b.getTotalPrice());
    }
}
