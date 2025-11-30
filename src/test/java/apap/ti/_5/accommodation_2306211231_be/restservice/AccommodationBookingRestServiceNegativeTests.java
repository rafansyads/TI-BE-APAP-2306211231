package apap.ti._5.accommodation_2306211231_be.restservice;

import static org.junit.jupiter.api.Assertions.*;
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
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingUpdateRequest;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccommodationBookingRestServiceNegativeTests {

    @org.junit.jupiter.api.BeforeEach
    void baseStubs() {
        when(customerService.findById(any(java.util.UUID.class))).thenAnswer(inv -> {
            java.util.UUID id = inv.getArgument(0);
            apap.ti._5.accommodation_2306211231_be.models.profile.Customer c = new apap.ti._5.accommodation_2306211231_be.models.profile.Customer();
            c.setId(id); c.setSaldo(1L);
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
        req.setTotalPrice(0);
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
    void createBooking_capacityOverflow_throws() {
        Room room = buildRoom(2);
        when(roomRepository.findById(room.getRoomId())).thenReturn(Optional.of(room));
        AccommodationBookingCreateRequest req = baseCreate(LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(3));
        req.setCapacity(99);
        assertThrows(IllegalArgumentException.class, () -> service.createBooking(req));
    }

    @Test
    void createBooking_maintenanceOverlap_throws() {
        Room room = buildRoom(2);
        // Fixed times to avoid flakiness when tests run after 16:00 local: maintenance 14:00-16:00 on target day
        LocalDateTime maintDay = LocalDateTime.now().plusDays(2);
        room.setMaintenanceStart(maintDay.withHour(14).withMinute(0).withSecond(0).withNano(0));
        room.setMaintenanceEnd(maintDay.withHour(16).withMinute(0).withSecond(0).withNano(0));
        when(roomRepository.findById(room.getRoomId())).thenReturn(Optional.of(room));
        // Explicit early-morning check-in so normalization forces 14:00 and overlaps with maintenance
        AccommodationBookingCreateRequest req = baseCreate(LocalDateTime.now().plusDays(2).withHour(8), LocalDateTime.now().plusDays(3).withHour(9));
        assertThrows(IllegalArgumentException.class, () -> service.createBooking(req));
    }

    @Test
    void createBooking_checkInBeforeNow_throws() {
        Room room = buildRoom(2);
        when(roomRepository.findById(room.getRoomId())).thenReturn(Optional.of(room));
        // Force a past check-in after normalization: choose yesterday at 10:00 (normalized to 14:00 yesterday)
        LocalDateTime yesterday = ZonedDateTime.now(ZoneId.of("Asia/Jakarta")).toLocalDate().minusDays(1).atTime(10,0);
        AccommodationBookingCreateRequest req = baseCreate(yesterday, yesterday.plusDays(1));
        assertThrows(IllegalArgumentException.class, () -> service.createBooking(req));
    }

    @Test
    void updateBooking_changesDatesTriggersDeltaStatusLogic() {
        Room room = buildRoom(2);
        AccommodationBooking existing = new AccommodationBooking();
        existing.setBookingId("BOOK-XYZ");
        existing.setRoom(room);
        existing.setCheckInDate(LocalDateTime.now().plusDays(5).withHour(14));
        existing.setCheckOutDate(LocalDateTime.now().plusDays(6).withHour(12));
        existing.setTotalDays(1);
        // Base price 100000 + breakfast 50,000 = 150,000 initial paid amount
        existing.setTotalPrice(150000);
        existing.setStatus(1); // paid
        existing.setCustomerId(UUID.randomUUID());
        existing.setCustomerName("Alice");
        existing.setCustomerEmail("alice@mail.com");
        existing.setCustomerPhone("+62-81234567890");
        existing.setIsBreakfast(Boolean.TRUE);
        when(bookingRepository.findById("BOOK-XYZ")).thenReturn(Optional.of(existing));
        when(roomRepository.findById(room.getRoomId())).thenReturn(Optional.of(room));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AccommodationBookingUpdateRequest req = new AccommodationBookingUpdateRequest();
        req.setBookingId("BOOK-XYZ");
        req.setRoomId(room.getRoomId());
        req.setRoomName("201");
        req.setRoomTypeName("Deluxe");
        req.setPropertyName("Hotel A");
        // shorter stay (1 -> still 1 due to computeDays min), adjust breakfast off to force negative delta
        req.setCheckInDate(existing.getCheckInDate());
        req.setCheckOutDate(existing.getCheckOutDate());
        req.setTotalDays(1);
    req.setTotalPrice(150000);
        req.setStatus(1);
        req.setCustomerId(existing.getCustomerId().toString());
        req.setCustomerName("Alice");
        req.setCustomerEmail("alice@mail.com");
        req.setCustomerPhone("+62-81234567890");
        req.setIsBreakfast(Boolean.FALSE); // remove breakfast -> cheaper
        req.setRefund(0);
        req.setExtraPay(0);
        req.setCapacity(2);

        var dto = service.updateBooking("BOOK-XYZ", req);
        // Paid booking with cheaper total should move to refund (status 3) per logic
        assertEquals(3, dto.getStatus());
        assertTrue(dto.getRefund() > 0);
    }
}
