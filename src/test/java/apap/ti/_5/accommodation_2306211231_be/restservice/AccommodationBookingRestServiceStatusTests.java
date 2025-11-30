package apap.ti._5.accommodation_2306211231_be.restservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import apap.ti._5.accommodation_2306211231_be.models.AccommodationBooking;
import apap.ti._5.accommodation_2306211231_be.models.Property;
import apap.ti._5.accommodation_2306211231_be.models.Room;
import apap.ti._5.accommodation_2306211231_be.models.RoomType;
import apap.ti._5.accommodation_2306211231_be.repository.AccommodationBookingRepository;
import apap.ti._5.accommodation_2306211231_be.repository.PropertyRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomRepository;

@ExtendWith(MockitoExtension.class)
class AccommodationBookingRestServiceStatusTests {

    @Mock private AccommodationBookingRepository bookingRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private PropertyRepository propertyRepository;

    @InjectMocks private AccommodationBookingRestService service;

    private Property property;
    private RoomType roomType;
    private Room room;

    @BeforeEach
    void setup() {
        property = Property.builder()
                .propertyId("PROP-001")
                .propertyName("King The Land")
                .type(1)
                .address("A")
                .province(31)
                .totalRoom(1)
                .activeStatus(1)
                .profit(2_000_000)
                .ownerName("O")
                .ownerId(UUID.randomUUID())
                .build();

        roomType = RoomType.builder()
                .roomTypeId("001-Deluxe-2")
                .name("Deluxe")
                .price(1_000_000)
                .capacity(2)
                .floor(2)
                .property(property)
                .build();

        room = Room.builder()
                .roomId("PROP-001-201")
                .name("201")
                .availabilityStatus(1)
                .activeRoom(1)
                .roomType(roomType)
                .build();
    }

    private AccommodationBooking baseBooking() {
        return AccommodationBooking.builder()
                .bookingId("BOOK-001-0010101")
                .checkInDate(java.time.LocalDateTime.now().plusDays(1))
                .checkOutDate(java.time.LocalDateTime.now().plusDays(2))
                .totalDays(1)
                .totalPrice(1_000_000)
                .status(0)
                .customerId(UUID.randomUUID())
                .customerName("Farrel")
                .customerEmail("f@example.com")
                .customerPhone("08")
                .isBreakfast(Boolean.TRUE)
                .refund(100_000)
                .extraPay(50_000)
                .capacity(2)
                .room(room)
                .build();
    }

    @Test
    void markAsPaid_appliesExtraMinusRefund_resetsDeltas_setsStatus1() {
        var booking = baseBooking(); // status 0 totalPrice 1_000_000 extra 50_000
        when(bookingRepository.findById(booking.getBookingId())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(propertyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var dto = service.markBookingAsPaid(booking.getBookingId());

        assertEquals(1, dto.getStatus());
        // Service clears extraPay on 0->1 transition but leaves refund as-is
        assertEquals(0, dto.getExtraPay());
        assertEquals(100_000, dto.getRefund());
        // profit += totalPrice + extraPay - refund => 2_000_000 + (1_000_000 + 50_000 - 100_000) = 2_950_000
        assertEquals(2_950_000, property.getProfit());
    }

    @Test
    void cancel_status0_noIncomeChange() {
        var booking = baseBooking(); // status 0
        when(bookingRepository.findById(booking.getBookingId())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(propertyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var dto = service.cancelBooking(booking.getBookingId());
        assertEquals(2, dto.getStatus());
        // status 0: Property.income unaffected
        assertEquals(2_000_000, property.getProfit());
    }

    @Test
    void cancel_status1_subtractsTotal() {
        var booking = baseBooking();
        booking.setStatus(1);
        when(bookingRepository.findById(booking.getBookingId())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(propertyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var dto = service.cancelBooking(booking.getBookingId());
        assertEquals(2, dto.getStatus());
        assertEquals(1_050_000, property.getProfit());
    }

    @Test
    void cancel_status3_subtractsTotalOnly() {
        var booking = baseBooking();
        booking.setStatus(3);
        when(bookingRepository.findById(booking.getBookingId())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(propertyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var dto = service.cancelBooking(booking.getBookingId());
        assertEquals(2, dto.getStatus());
        // new rule: subtract only the recognized total
        assertEquals(2_000_000 - 1_000_000, property.getProfit());
    }

    @Test
    void refund_fromPaid_subtractsRefund_setsStatus1() {
        var booking = baseBooking();
        booking.setStatus(1);
        when(bookingRepository.findById(booking.getBookingId())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(propertyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var dto = service.refundBooking(booking.getBookingId());
        // After refund processed, status goes back to paid (1)
        assertEquals(1, dto.getStatus());
        assertEquals(2_000_000 - 100_000, property.getProfit());
    }
}
