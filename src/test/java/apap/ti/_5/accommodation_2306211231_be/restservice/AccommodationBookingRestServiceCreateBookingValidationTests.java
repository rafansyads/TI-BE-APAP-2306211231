package apap.ti._5.accommodation_2306211231_be.restservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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

import apap.ti._5.accommodation_2306211231_be.models.*;
import apap.ti._5.accommodation_2306211231_be.repository.AccommodationBookingRepository;
import apap.ti._5.accommodation_2306211231_be.repository.PropertyRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomRepository;
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingCreateRequest;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccommodationBookingRestServiceCreateBookingValidationTests {

    @BeforeEach
    void baseStubs() {
        when(customerService.findById(any(java.util.UUID.class))).thenAnswer(inv -> {
            java.util.UUID id = inv.getArgument(0);
            apap.ti._5.accommodation_2306211231_be.models.profile.Customer c = new apap.ti._5.accommodation_2306211231_be.models.profile.Customer();
            c.setId(id);
            c.setSaldo(1L);
            return java.util.Optional.of(c);
        });
    }

    @Mock AccommodationBookingRepository bookingRepository;
    @Mock RoomRepository roomRepository;
    @Mock PropertyRepository propertyRepository;
    @Mock apap.ti._5.accommodation_2306211231_be.restservice.CustomerRestService customerService;
    @InjectMocks AccommodationBookingRestService service;

    private Room buildRoom(int capacity) {
        Property prop = Property.builder().propertyId("HOT-ABCD-001").propertyName("Hotel A").profit(0).build();
        RoomType rt = RoomType.builder().roomTypeId("001-Deluxe-2").name("Deluxe").price(100000).capacity(capacity).property(prop).build();
        Room room = Room.builder().roomId("HOT-ABCD-001-201").name("201").roomType(rt).availabilityStatus(1).activeRoom(1).build();
        rt.setListRoom(List.of(room));
        prop.setListRoomType(List.of(rt));
        return room;
    }

    private AccommodationBookingCreateRequest baseCreate(LocalDateTime in, LocalDateTime out) {
        AccommodationBookingCreateRequest req = new AccommodationBookingCreateRequest();
        req.setRoomId("HOT-ABCD-001-201");
        req.setCheckInDate(in);
        req.setCheckOutDate(out);
        req.setTotalDays(0);
        req.setTotalPrice(999999); // will be overridden by service
        req.setStatus(0);
        req.setCustomerId(UUID.randomUUID().toString());
        req.setCustomerName("Alice");
        req.setCustomerEmail("alice@mail.com");
        req.setCustomerPhone("081234567890");
        req.setIsBreakfast(Boolean.TRUE);
        req.setRefund(0);
        req.setExtraPay(0);
        req.setCapacity(2);
        return req;
    }


    @Test
    void createBooking_nullRequest_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.createBooking((AccommodationBookingCreateRequest) null));
    }

    @Test
    void createBooking_missingRoomId_throws() {
        var req = baseCreate(LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(4));
        req.setRoomId(null);
        var ex = assertThrows(IllegalArgumentException.class, () -> service.createBooking(req));
        assertTrue(ex.getMessage().toLowerCase().contains("roomid is required"));
    }

    @Test
    void createBooking_roomNotFound_throws() {
        when(roomRepository.findById(anyString())).thenReturn(Optional.empty());
        var req = baseCreate(LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(4));
        var ex = assertThrows(IllegalArgumentException.class, () -> service.createBooking(req));
        assertTrue(ex.getMessage().toLowerCase().contains("room not found"));
    }

    @Test
    void createBooking_roomNameMismatch_throws() {
        Room room = buildRoom(2);
        when(roomRepository.findById(room.getRoomId())).thenReturn(Optional.of(room));
        var req = baseCreate(LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(4));
        req.setRoomName("202");
        assertThrows(IllegalArgumentException.class, () -> service.createBooking(req));
    }

    @Test
    void createBooking_roomTypeNameMismatch_throws() {
        Room room = buildRoom(2);
        when(roomRepository.findById(room.getRoomId())).thenReturn(Optional.of(room));
        var req = baseCreate(LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(4));
        req.setRoomTypeName("Suite");
        assertThrows(IllegalArgumentException.class, () -> service.createBooking(req));
    }

    @Test
    void createBooking_propertyNameMismatch_throws() {
        Room room = buildRoom(2);
        when(roomRepository.findById(room.getRoomId())).thenReturn(Optional.of(room));
        var req = baseCreate(LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(4));
        req.setPropertyName("Another");
        assertThrows(IllegalArgumentException.class, () -> service.createBooking(req));
    }

    @Test
    void createBooking_invalidStayWindow_throws() {
        Room room = buildRoom(2);
        when(roomRepository.findById(room.getRoomId())).thenReturn(Optional.of(room));
        // same day checkout earlier than check-in after normalization
        var req = baseCreate(LocalDateTime.now().plusDays(3).withHour(16), LocalDateTime.now().plusDays(3).withHour(9));
        assertThrows(IllegalArgumentException.class, () -> service.createBooking(req));
    }

    @Test
    void createBooking_existingBookingOverlap_throws() {
        Room room = buildRoom(2);
        AccommodationBooking existing = new AccommodationBooking();
        existing.setBookingId("B-OL");
        existing.setCheckInDate(LocalDateTime.now().plusDays(5).withHour(14));
        existing.setCheckOutDate(LocalDateTime.now().plusDays(7).withHour(12));
        existing.setStatus(1);
        room.setBookings(new java.util.ArrayList<>(List.of(existing)));
        when(roomRepository.findById(room.getRoomId())).thenReturn(Optional.of(room));
        var req = baseCreate(LocalDateTime.now().plusDays(6), LocalDateTime.now().plusDays(8));
        assertThrows(IllegalArgumentException.class, () -> service.createBooking(req));
    }

    @Test
    void createBooking_bookingIdMismatch_throws() {
        Room room = buildRoom(2);
        when(roomRepository.findById(room.getRoomId())).thenReturn(Optional.of(room));
        var req = baseCreate(LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(4));
        req.setBookingId("FORCED-ID");
        var ex = assertThrows(IllegalArgumentException.class, () -> service.createBooking(req));
        assertTrue(ex.getMessage().toLowerCase().contains("bookingid mismatch"));
    }

    @Test
    void createBooking_success_overridesClientTotal_andBreakfastPricing() {
        Room room = buildRoom(2);
        when(roomRepository.findById(room.getRoomId())).thenReturn(Optional.of(room));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        var in = LocalDateTime.now().plusDays(2);
        var out = LocalDateTime.now().plusDays(4);
        var req = baseCreate(in, out);
        // client tries to set wrong totals
        req.setTotalPrice(1);
        var dto = service.createBooking(req);
        // 2 days * (100000 base + 50000 breakfast)
        assertEquals(2 * (100000 + 50000), dto.getTotalPrice());
        assertEquals(0, dto.getExtraPay());
        assertNotNull(dto.getBookingId());
    }
}
