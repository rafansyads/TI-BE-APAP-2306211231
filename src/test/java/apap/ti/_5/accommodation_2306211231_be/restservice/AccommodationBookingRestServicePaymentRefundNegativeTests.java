package apap.ti._5.accommodation_2306211231_be.restservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

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
class AccommodationBookingRestServicePaymentRefundNegativeTests {

    @Mock AccommodationBookingRepository bookingRepository;
    @Mock RoomRepository roomRepository;
    @Mock PropertyRepository propertyRepository;
    @Mock apap.ti._5.accommodation_2306211231_be.restservice.CustomerRestService customerService;
    @InjectMocks AccommodationBookingRestService service;

    private AccommodationBooking bookingWithProperty(int status, int refund) {
        Property prop = Property.builder().propertyId("HOT-X").propertyName("Hotel X").type(1).address("A").province(1).activeStatus(1).totalRoom(0).ownerName("O").ownerId(java.util.UUID.randomUUID()).profit(0).build();
        RoomType rt = RoomType.builder().roomTypeId("RT-X").name("Std").price(100000).capacity(2).floor(1).property(prop).build();
        Room r = Room.builder().roomId("RM-X").name("101").roomType(rt).availabilityStatus(1).activeRoom(1).build();
        var b = new AccommodationBooking();
        b.setBookingId("B-1"); b.setRoom(r); b.setStatus(status); b.setRefund(refund); b.setTotalPrice(100000);
        return b;
    }

    @Test
    void markPaid_nonWaitingStatus_throws() {
        var b = bookingWithProperty(1,0); // already paid
        when(bookingRepository.findById("B-1")).thenReturn(Optional.of(b));
        var ex = assertThrows(IllegalStateException.class, () -> service.markBookingAsPaid("B-1"));
        assertTrue(ex.getMessage().toLowerCase().contains("only bookings with status 0"));
    }

    @Test
    void refundBooking_invalidStatus_throws() {
        var b = bookingWithProperty(0, 10); // waiting but refund set
        when(bookingRepository.findById("B-1")).thenReturn(Optional.of(b));
        var ex = assertThrows(IllegalStateException.class, () -> service.refundBooking("B-1"));
        assertTrue(ex.getMessage().toLowerCase().contains("refund can only"));
    }

    @Test
    void refundBooking_zeroRefund_throws() {
        var b = bookingWithProperty(1, 0); // paid but no refund
        when(bookingRepository.findById("B-1")).thenReturn(Optional.of(b));
        var ex = assertThrows(IllegalArgumentException.class, () -> service.refundBooking("B-1"));
        assertTrue(ex.getMessage().toLowerCase().contains("refund amount"));
    }
}
