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
import apap.ti._5.accommodation_2306211231_be.repository.AccommodationBookingRepository;
import apap.ti._5.accommodation_2306211231_be.repository.PropertyRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomRepository;
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingUpdateRequest;

@ExtendWith(MockitoExtension.class)
class AccommodationBookingRestServiceUpdateNegativeTests {

    @Mock AccommodationBookingRepository bookingRepository;
    @Mock RoomRepository roomRepository;
    @Mock PropertyRepository propertyRepository;
    @Mock apap.ti._5.accommodation_2306211231_be.restservice.CustomerRestService customerService;
    @InjectMocks AccommodationBookingRestService service;

    @Test
    void updateBooking_pathBodyMismatch_throws() {
        var req = new AccommodationBookingUpdateRequest();
        req.setBookingId("B-2");
        var ex = assertThrows(IllegalArgumentException.class, () -> service.updateBooking("B-1", req));
        assertTrue(ex.getMessage().contains("must match"));
    }

    @Test
    void updateBooking_status2Rejected() {
        AccommodationBooking existing = new AccommodationBooking();
        existing.setBookingId("B-1"); existing.setStatus(2); existing.setExtraPay(0); existing.setRefund(0);
        when(bookingRepository.findById("B-1")).thenReturn(Optional.of(existing));
        var req = new AccommodationBookingUpdateRequest();
        req.setBookingId("B-1");
        var ex = assertThrows(IllegalStateException.class, () -> service.updateBooking("B-1", req));
        assertTrue(ex.getMessage().contains("status 2 or 4"));
    }

    @Test
    void updateBooking_status3Rejected() {
        AccommodationBooking existing = new AccommodationBooking();
        existing.setBookingId("B-1"); existing.setStatus(3); existing.setExtraPay(0); existing.setRefund(0);
        when(bookingRepository.findById("B-1")).thenReturn(Optional.of(existing));
        var req = new AccommodationBookingUpdateRequest(); req.setBookingId("B-1");
        var ex = assertThrows(IllegalStateException.class, () -> service.updateBooking("B-1", req));
        assertTrue(ex.getMessage().toLowerCase().contains("refund state"));
    }

    @Test
    void updateBooking_pendingExtraOrRefundRejected() {
        AccommodationBooking existing = new AccommodationBooking();
        existing.setBookingId("B-1"); existing.setStatus(1); existing.setExtraPay(10); existing.setRefund(0);
        when(bookingRepository.findById("B-1")).thenReturn(Optional.of(existing));
        var req = new AccommodationBookingUpdateRequest(); req.setBookingId("B-1");
        var ex = assertThrows(IllegalStateException.class, () -> service.updateBooking("B-1", req));
        assertTrue(ex.getMessage().toLowerCase().contains("pending extra payment"));
    }
}
