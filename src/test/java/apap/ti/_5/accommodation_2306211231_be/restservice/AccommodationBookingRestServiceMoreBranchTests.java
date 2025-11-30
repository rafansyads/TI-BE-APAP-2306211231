package apap.ti._5.accommodation_2306211231_be.restservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

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
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingUpdateRequest;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccommodationBookingRestServiceMoreBranchTests {

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

    private AccommodationBooking baseBooking(String id, int status) {
        Property p = Property.builder().propertyId("HOT-ABCD-001").propertyName("Hotel A").profit(0).build();
        RoomType rt = RoomType.builder().roomTypeId("RT-1").name("Deluxe").price(200000).capacity(2).floor(1).property(p).build();
        Room room = Room.builder().roomId("RM-1").name("101").roomType(rt).availabilityStatus(1).activeRoom(1).build();
        AccommodationBooking b = new AccommodationBooking();
        b.setBookingId(id);
        b.setStatus(status);
        b.setCustomerId(UUID.randomUUID());
        b.setCheckInDate(LocalDateTime.now().plusDays(3));
        b.setCheckOutDate(LocalDateTime.now().plusDays(4));
        b.setIsBreakfast(false);
        b.setTotalPrice(200000); // baseline price (1 day * 200000)
        b.setTotalDays(1);
        b.setExtraPay(0); b.setRefund(0);
        b.setCapacity(2);
        b.setRoom(room);
        return b;
    }

    @Test
    void updateBooking_roomTypeNameMismatch_throws() {
        var existing = baseBooking("B-X", 0);
        when(bookingRepository.findById("B-X")).thenReturn(Optional.of(existing));
        when(roomRepository.findById("RM-1")).thenReturn(Optional.of(existing.getRoom()));
        var req = new AccommodationBookingUpdateRequest();
        req.setBookingId("B-X"); req.setRoomId("RM-1");
        req.setRoomName("101");
        req.setRoomTypeName("WRONG-TYPE"); // mismatch
        req.setPropertyName("Hotel A");
        req.setCustomerId(existing.getCustomerId().toString());
        req.setCheckInDate(existing.getCheckInDate());
        req.setCheckOutDate(existing.getCheckOutDate().plusDays(1));
        req.setIsBreakfast(false); req.setCapacity(2);
        var ex = assertThrows(IllegalArgumentException.class, () -> service.updateBooking("B-X", req));
        assertTrue(ex.getMessage().toLowerCase().contains("roomtypename does not match"));
    }

    @Test
    void updateBooking_propertyNameMismatch_throws() {
        var existing = baseBooking("B-Y", 0);
        when(bookingRepository.findById("B-Y")).thenReturn(Optional.of(existing));
        when(roomRepository.findById("RM-1")).thenReturn(Optional.of(existing.getRoom()));
        var req = new AccommodationBookingUpdateRequest();
        req.setBookingId("B-Y"); req.setRoomId("RM-1");
        req.setRoomName("101"); req.setRoomTypeName("Deluxe");
        req.setPropertyName("Other Hotel"); // mismatch
        req.setCustomerId(existing.getCustomerId().toString());
        req.setCheckInDate(existing.getCheckInDate());
        req.setCheckOutDate(existing.getCheckOutDate().plusDays(1));
        req.setIsBreakfast(false); req.setCapacity(2);
        var ex = assertThrows(IllegalArgumentException.class, () -> service.updateBooking("B-Y", req));
        assertTrue(ex.getMessage().toLowerCase().contains("propertyname does not match"));
    }

    @Test
    void cancelBooking_invalidStatus_throws() {
        var existing = baseBooking("B-Z", 4); // status 4 done
        when(bookingRepository.findById("B-Z")).thenReturn(Optional.of(existing));
        var ex = assertThrows(IllegalStateException.class, () -> service.cancelBooking("B-Z"));
        assertTrue(ex.getMessage().toLowerCase().contains("only bookings with status 0, 1, or 3"));
    }
}
