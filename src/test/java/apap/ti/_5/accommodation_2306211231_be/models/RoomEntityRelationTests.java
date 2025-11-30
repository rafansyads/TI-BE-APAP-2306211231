package apap.ti._5.accommodation_2306211231_be.models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

class RoomEntityRelationTests {

    @Test
    void addBooking_setsRoomAndAddsToList() {
        RoomType rt = RoomType.builder().roomTypeId("RT-1").name("Type").build();
        Room room = Room.builder().roomId("R-1").name("R1").availabilityStatus(1).activeRoom(1).roomType(rt).build();
        AccommodationBooking booking = new AccommodationBooking();
        booking.setBookingId("B-1");
        booking.setCheckInDate(LocalDateTime.now().plusDays(2));
        booking.setCheckOutDate(LocalDateTime.now().plusDays(3));

        room.addBooking(booking);

        assertEquals(1, room.getBookings().size());
        assertSame(room, booking.getRoom());
    }

    @Test
    void removeBooking_unsetsRoomAndRemovesFromList() {
        RoomType rt = RoomType.builder().roomTypeId("RT-1").name("Type").build();
        Room room = Room.builder().roomId("R-1").name("R1").availabilityStatus(1).activeRoom(1).roomType(rt).build();
        AccommodationBooking booking = new AccommodationBooking();
        booking.setBookingId("B-1");
        room.addBooking(booking);

        room.removeBooking(booking);

        assertTrue(room.getBookings().isEmpty());
        assertNull(booking.getRoom());
    }
}
