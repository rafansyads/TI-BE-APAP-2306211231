package apap.ti._5.accommodation_2306211231_be.restservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccommodationBookingRestServiceChartTests {

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
    @Mock RoomRepository roomRepository; // not used directly
    @Mock PropertyRepository propertyRepository;
        @Mock apap.ti._5.accommodation_2306211231_be.restservice.CustomerRestService customerService;
    @InjectMocks AccommodationBookingRestService service;

    @Test
    void getBookingChartAggregatesIncomeByProperty() {
        Property p1 = Property.builder().propertyId("HOT-ABCD-001").propertyName("Hotel A").profit(0).type(1).build();
        Property p2 = Property.builder().propertyId("HOT-EFGH-002").propertyName("Hotel B").profit(0).type(2).build();

        RoomType rt1 = RoomType.builder().roomTypeId("001-Std-1").name("Std").price(100000).property(p1).build();
        RoomType rt2 = RoomType.builder().roomTypeId("002-Std-1").name("Std").price(150000).property(p2).build();
        Room r1 = Room.builder().roomId("HOT-ABCD-001-101").name("101").roomType(rt1).build();
        Room r2 = Room.builder().roomId("HOT-EFGH-002-201").name("201").roomType(rt2).build();
        rt1.setListRoom(List.of(r1));
        rt2.setListRoom(List.of(r2));
        p1.setListRoomType(List.of(rt1));
        p2.setListRoomType(List.of(rt2));

        // Bookings for given month/year
        LocalDateTime in1 = LocalDateTime.of(2025, 5, 10, 14, 0);
        LocalDateTime out1 = LocalDateTime.of(2025, 5, 11, 12, 0);
        AccommodationBooking b1 = AccommodationBooking.builder()
                .bookingId("B1")
                .room(r1)
                .checkInDate(in1)
                .checkOutDate(out1)
                .status(1) // paid
                .totalPrice(100000)
                .customerId(UUID.randomUUID())
                .build();

        LocalDateTime in2 = LocalDateTime.of(2025, 5, 15, 14, 0);
        LocalDateTime out2 = LocalDateTime.of(2025, 5, 17, 12, 0);
        AccommodationBooking b2 = AccommodationBooking.builder()
                .bookingId("B2")
                .room(r2)
                .checkInDate(in2)
                .checkOutDate(out2)
                .status(1) // paid
                .totalPrice(300000)
                .customerId(UUID.randomUUID())
                .build();

        when(propertyRepository.findByDeletedAtIsNull()).thenReturn(List.of(p1, p2));
        when(bookingRepository.findAll()).thenReturn(List.of(b1, b2));

        Map<String, Object> chart = service.getBookingChart(5, 2025, null);
        assertEquals(5, chart.get("month"));
        assertEquals(2025, chart.get("year"));
        @SuppressWarnings("unchecked") List<String> labels = (List<String>) chart.get("labels");
        @SuppressWarnings("unchecked") List<Integer> data = (List<Integer>) chart.get("data");
        assertEquals(2, labels.size());
        assertEquals(List.of("Hotel A", "Hotel B"), labels);
        assertEquals(List.of(100000, 300000), data);
    }
}
