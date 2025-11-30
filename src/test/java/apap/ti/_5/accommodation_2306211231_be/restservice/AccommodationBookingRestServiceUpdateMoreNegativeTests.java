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

import apap.ti._5.accommodation_2306211231_be.models.*;
import apap.ti._5.accommodation_2306211231_be.repository.*;
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingUpdateRequest;

@ExtendWith(MockitoExtension.class)
class AccommodationBookingRestServiceUpdateMoreNegativeTests {

    @Mock AccommodationBookingRepository bookingRepository;
    @Mock RoomRepository roomRepository;
    @Mock PropertyRepository propertyRepository;
    @Mock apap.ti._5.accommodation_2306211231_be.restservice.CustomerRestService customerService;
    @InjectMocks AccommodationBookingRestService service;

    private AccommodationBooking minimalExisting(String id, int status) {
        AccommodationBooking b = new AccommodationBooking();
        b.setBookingId(id);
        b.setStatus(status);
        b.setExtraPay(0); b.setRefund(0);
        b.setCustomerId(UUID.randomUUID());
        b.setCheckInDate(LocalDateTime.now().plusDays(3));
        b.setCheckOutDate(LocalDateTime.now().plusDays(4));
        RoomType rt = RoomType.builder().roomTypeId("RT-1").name("Deluxe").price(100000).capacity(2).floor(1)
            .property(Property.builder().propertyId("P-1").propertyName("Hotel A").type(1).address("A").province(1).activeStatus(1).totalRoom(0).ownerName("O").ownerId(UUID.randomUUID()).profit(0).build())
            .build();
        Room r = Room.builder().roomId("RM-1").name("101").roomType(rt).availabilityStatus(1).activeRoom(1).build();
        b.setRoom(r);
        return b;
    }

    @Test
    void updateBooking_customerIdChanged_throws() {
        var existing = minimalExisting("B-1", 0);
        when(bookingRepository.findById("B-1")).thenReturn(Optional.of(existing));
        // Do NOT stub roomRepository here; the service will throw before resolving the room
        AccommodationBookingUpdateRequest req = new AccommodationBookingUpdateRequest();
        req.setBookingId("B-1");
        req.setRoomId("RM-1");
        req.setRoomName("101"); req.setRoomTypeName("Deluxe"); req.setPropertyName("Hotel A");
        req.setCustomerId(UUID.randomUUID().toString()); // different
        req.setCheckInDate(existing.getCheckInDate());
        req.setCheckOutDate(existing.getCheckOutDate().plusDays(1));
        req.setIsBreakfast(false); req.setCapacity(2);
        var ex = assertThrows(IllegalArgumentException.class, () -> service.updateBooking("B-1", req));
        assertTrue(ex.getMessage().toLowerCase().contains("customerid"));
    }

    @Test
    void updateBooking_roomIdMissing_throws() {
        var existing = minimalExisting("B-1", 0);
        when(bookingRepository.findById("B-1")).thenReturn(Optional.of(existing));
        AccommodationBookingUpdateRequest req = new AccommodationBookingUpdateRequest();
        req.setBookingId("B-1");
        var ex = assertThrows(IllegalArgumentException.class, () -> service.updateBooking("B-1", req));
        assertTrue(ex.getMessage().toLowerCase().contains("roomid is required"));
    }

    @Test
    void updateBooking_roomNotFound_throws() {
        var existing = minimalExisting("B-1", 0);
        when(bookingRepository.findById("B-1")).thenReturn(Optional.of(existing));
        when(roomRepository.findById("NOPE")).thenReturn(Optional.empty());
        AccommodationBookingUpdateRequest req = new AccommodationBookingUpdateRequest();
        req.setBookingId("B-1"); req.setRoomId("NOPE");
        var ex = assertThrows(IllegalArgumentException.class, () -> service.updateBooking("B-1", req));
        assertTrue(ex.getMessage().toLowerCase().contains("room not found"));
    }

    @Test
    void updateBooking_missingNames_throws() {
        var existing = minimalExisting("B-1", 0);
        when(bookingRepository.findById("B-1")).thenReturn(Optional.of(existing));
        when(roomRepository.findById("RM-1")).thenReturn(Optional.of(existing.getRoom()));
        AccommodationBookingUpdateRequest req = new AccommodationBookingUpdateRequest();
        req.setBookingId("B-1"); req.setRoomId("RM-1");
        var ex = assertThrows(IllegalArgumentException.class, () -> service.updateBooking("B-1", req));
        assertTrue(ex.getMessage().toLowerCase().contains("required"));
    }

    @Test
    void updateBooking_nameMismatch_throws() {
        var existing = minimalExisting("B-1", 0);
        when(bookingRepository.findById("B-1")).thenReturn(Optional.of(existing));
        when(roomRepository.findById("RM-1")).thenReturn(Optional.of(existing.getRoom()));
        AccommodationBookingUpdateRequest req = new AccommodationBookingUpdateRequest();
        req.setBookingId("B-1"); req.setRoomId("RM-1");
        req.setRoomName("WRONG"); req.setRoomTypeName("Deluxe"); req.setPropertyName("Hotel A");
        req.setCustomerId(existing.getCustomerId().toString());
        req.setCheckInDate(existing.getCheckInDate()); req.setCheckOutDate(existing.getCheckOutDate().plusDays(1));
        req.setIsBreakfast(false); req.setCapacity(2);
        var ex = assertThrows(IllegalArgumentException.class, () -> service.updateBooking("B-1", req));
        assertTrue(ex.getMessage().toLowerCase().contains("roomname does not match"));
    }

    @Test
    void updateBooking_invalidWindow_throws() {
        var existing = minimalExisting("B-1", 0);
        when(bookingRepository.findById("B-1")).thenReturn(Optional.of(existing));
        when(roomRepository.findById("RM-1")).thenReturn(Optional.of(existing.getRoom()));
        AccommodationBookingUpdateRequest req = new AccommodationBookingUpdateRequest();
        req.setBookingId("B-1"); req.setRoomId("RM-1");
        req.setRoomName("101"); req.setRoomTypeName("Deluxe"); req.setPropertyName("Hotel A");
        req.setCustomerId(existing.getCustomerId().toString());
    req.setCustomerPhone("081234567890");
        req.setCheckInDate(LocalDateTime.now().plusDays(4));
        req.setCheckOutDate(LocalDateTime.now().plusDays(3)); // out before in
        req.setIsBreakfast(false); req.setCapacity(2);
        var ex = assertThrows(IllegalArgumentException.class, () -> service.updateBooking("B-1", req));
        assertTrue(ex.getMessage().contains("Invalid stay window"), "Expected invalid stay window message but was: " + ex.getMessage());
    }

    @Test
    void updateBooking_pastCheckIn_throws() {
        var existing = minimalExisting("B-1", 0);
        when(bookingRepository.findById("B-1")).thenReturn(Optional.of(existing));
        when(roomRepository.findById("RM-1")).thenReturn(Optional.of(existing.getRoom()));
        AccommodationBookingUpdateRequest req = new AccommodationBookingUpdateRequest();
        req.setBookingId("B-1"); req.setRoomId("RM-1");
        req.setRoomName("101"); req.setRoomTypeName("Deluxe"); req.setPropertyName("Hotel A");
        req.setCustomerId(existing.getCustomerId().toString());
    req.setCustomerPhone("081234567890");
        req.setCheckInDate(LocalDateTime.now().minusDays(1));
        req.setCheckOutDate(LocalDateTime.now().plusDays(1));
        req.setIsBreakfast(false); req.setCapacity(2);
        var ex = assertThrows(IllegalArgumentException.class, () -> service.updateBooking("B-1", req));
        assertTrue(ex.getMessage().contains("earlier than now"), "Expected past check-in message but was: " + ex.getMessage());
    }

    @Test
    void updateBooking_maintenanceOverlap_throws() {
        var existing = minimalExisting("B-1", 0);
        existing.getRoom().setMaintenanceStart(LocalDateTime.now().plusDays(3).withHour(14));
        existing.getRoom().setMaintenanceEnd(LocalDateTime.now().plusDays(4).withHour(12));
        when(bookingRepository.findById("B-1")).thenReturn(Optional.of(existing));
        when(roomRepository.findById("RM-1")).thenReturn(Optional.of(existing.getRoom()));
        AccommodationBookingUpdateRequest req = new AccommodationBookingUpdateRequest();
        req.setBookingId("B-1"); req.setRoomId("RM-1");
        req.setRoomName("101"); req.setRoomTypeName("Deluxe"); req.setPropertyName("Hotel A");
        req.setCustomerId(existing.getCustomerId().toString());
    req.setCustomerPhone("081234567890");
        req.setCheckInDate(existing.getCheckInDate()); req.setCheckOutDate(existing.getCheckOutDate().plusDays(1));
        req.setIsBreakfast(false); req.setCapacity(2);
        var ex = assertThrows(IllegalArgumentException.class, () -> service.updateBooking("B-1", req));
        assertTrue(ex.getMessage().contains("maintenance schedule"), "Expected maintenance overlap message but was: " + ex.getMessage());
    }

    @Test
    void updateBooking_existingBookingOverlap_throws() {
        var existing = minimalExisting("B-1", 0);
        AccommodationBooking other = new AccommodationBooking();
        other.setBookingId("B-2"); other.setStatus(1);
        other.setCheckInDate(existing.getCheckInDate());
        other.setCheckOutDate(existing.getCheckOutDate());
        existing.getRoom().setBookings(java.util.List.of(existing, other));
        when(bookingRepository.findById("B-1")).thenReturn(Optional.of(existing));
        when(roomRepository.findById("RM-1")).thenReturn(Optional.of(existing.getRoom()));
        AccommodationBookingUpdateRequest req = new AccommodationBookingUpdateRequest();
        req.setBookingId("B-1"); req.setRoomId("RM-1");
        req.setRoomName("101"); req.setRoomTypeName("Deluxe"); req.setPropertyName("Hotel A");
        req.setCustomerId(existing.getCustomerId().toString());
    req.setCustomerPhone("081234567890");
        // Set overlapping window
        req.setCheckInDate(existing.getCheckInDate());
        req.setCheckOutDate(existing.getCheckOutDate());
        req.setIsBreakfast(false); req.setCapacity(2);
        var ex = assertThrows(IllegalArgumentException.class, () -> service.updateBooking("B-1", req));
        assertTrue(ex.getMessage().contains("overlaps with an existing booking"), "Expected existing booking overlap message but was: " + ex.getMessage());
    }

    @Test
    void updateBooking_capacityExceeded_throws() {
        var existing = minimalExisting("B-1", 0);
        when(bookingRepository.findById("B-1")).thenReturn(Optional.of(existing));
        when(roomRepository.findById("RM-1")).thenReturn(Optional.of(existing.getRoom()));
        AccommodationBookingUpdateRequest req = new AccommodationBookingUpdateRequest();
        req.setBookingId("B-1"); req.setRoomId("RM-1");
        req.setRoomName("101"); req.setRoomTypeName("Deluxe"); req.setPropertyName("Hotel A");
        req.setCustomerId(existing.getCustomerId().toString());
        req.setCustomerPhone("081234567890");
        req.setCheckInDate(existing.getCheckInDate()); req.setCheckOutDate(existing.getCheckOutDate().plusDays(1));
        req.setIsBreakfast(false); req.setCapacity(5); // exceeds rt capacity 2
        var ex = assertThrows(IllegalArgumentException.class, () -> service.updateBooking("B-1", req));
        assertTrue(ex.getMessage().contains("Requested capacity exceeds"), "Expected capacity exceeds message but was: " + ex.getMessage());
    }
}
