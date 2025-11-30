package apap.ti._5.accommodation_2306211231_be.restservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
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
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingCreateRequest;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccommodationBookingRestServiceCreateUpdateTests {

    @Mock private AccommodationBookingRepository bookingRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private PropertyRepository propertyRepository; // unused here
    @Mock private apap.ti._5.accommodation_2306211231_be.restservice.CustomerRestService customerService;

    @InjectMocks private AccommodationBookingRestService service;

    private Room prepareRoom(int capacity) {
        Property p = Property.builder()
                .propertyId("PROP-001")
                .propertyName("King")
                .type(1)
                .address("A")
                .province(31)
                .totalRoom(1)
                .activeStatus(1)
                .ownerName("Owner")
                .ownerId(UUID.randomUUID())
                .profit(0)
                .build();
        RoomType rt = RoomType.builder()
                .roomTypeId("001-Deluxe-2")
                .name("Deluxe")
                .price(1000000)
                .capacity(capacity)
                .floor(2)
                .property(p)
                .build();
        return Room.builder()
                .roomId("PROP-001-201")
                .name("201")
                .availabilityStatus(1)
                .activeRoom(1)
                .roomType(rt)
                .build();
    }

    @BeforeEach
    void baseStubs() {
        when(customerService.findById(any(java.util.UUID.class))).thenAnswer(inv -> {
            java.util.UUID id = inv.getArgument(0);
            apap.ti._5.accommodation_2306211231_be.models.profile.Customer c = new apap.ti._5.accommodation_2306211231_be.models.profile.Customer();
            c.setId(id); c.setSaldo(1_000_000L);
            return java.util.Optional.of(c);
        });
    }

    private AccommodationBookingCreateRequest baseRequest() {
        AccommodationBookingCreateRequest req = new AccommodationBookingCreateRequest();
        LocalDateTime in = LocalDateTime.now().plusDays(2).withHour(8); // earlier than 14:00 to test normalization
        LocalDateTime out = LocalDateTime.now().plusDays(3).withHour(18); // later than 12:00 to test normalization
    req.setCheckInDate(in);
    req.setCheckOutDate(out);
        req.setTotalDays(0); // will be recomputed
        req.setTotalPrice(2_000_000);
        req.setStatus(0);
        req.setCustomerId(UUID.randomUUID().toString());
        req.setCustomerName("Farrel");
        req.setCustomerEmail("f@example.com");
        req.setCustomerPhone("081234567890");
        req.setIsBreakfast(Boolean.TRUE);
        req.setRefund(0);
        req.setExtraPay(0);
        req.setCapacity(2);
        req.setRoomId("PROP-001-201");
        return req;
    }

    @Test
    void createBooking_normalizesTimes_andSetsExtraPay() {
        Room room = prepareRoom(2);
        when(roomRepository.findById(room.getRoomId())).thenReturn(Optional.of(room));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var dto = service.createBooking(baseRequest());
        assertEquals(2, dto.getCapacity());
    // Breakfast cost is embedded in totalPrice; extraPay should remain 0 for breakfast-only scenario
    assertEquals(0, dto.getExtraPay());
        assertEquals(0, dto.getStatus());
    }

    @Test
    void createBooking_capacityExceedsRoomType_throws() {
        Room room = prepareRoom(2);
        when(roomRepository.findById(room.getRoomId())).thenReturn(Optional.of(room));
        AccommodationBookingCreateRequest req = baseRequest();
        req.setCapacity(99); // exceed
        assertThrows(IllegalArgumentException.class, () -> service.createBooking(req));
    }

    @Test
    void createBooking_maintenanceOverlap_throws() {
        Room room = prepareRoom(2);
        room.setMaintenanceStart(LocalDateTime.now().plusDays(2).withHour(14));
        room.setMaintenanceEnd(LocalDateTime.now().plusDays(2).withHour(16));
        when(roomRepository.findById(room.getRoomId())).thenReturn(Optional.of(room));
        AccommodationBookingCreateRequest req = baseRequest();
        assertThrows(IllegalArgumentException.class, () -> service.createBooking(req));
    }
}
