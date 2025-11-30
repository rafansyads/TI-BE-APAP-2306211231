package apap.ti._5.accommodation_2306211231_be.restmapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import apap.ti._5.accommodation_2306211231_be.models.*;
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.response.accommodationbooking.AccommodationBookingDto;

class AccommodationBookingMapperIT {

    @Test
    void fromCreateRequest_then_toDto_roundTrip_basicFields() {
        AccommodationBookingCreateRequest req = new AccommodationBookingCreateRequest();
        req.setRoomId("RM-1");
        req.setCheckInDate(LocalDateTime.now().plusDays(2));
        req.setCheckOutDate(LocalDateTime.now().plusDays(3));
        req.setCustomerId(UUID.randomUUID().toString());
        req.setCustomerName("Alice");
        req.setCustomerEmail("a@a.com");
    req.setCustomerPhone("081234567890");
        req.setIsBreakfast(Boolean.TRUE);
        req.setCapacity(2);

        AccommodationBooking entity = AccommodationBookingMapper.fromCreateRequest(req);
        entity.setBookingId("BK-1");
        RoomType rt = RoomType.builder().roomTypeId("RT-1").name("Deluxe").price(100000).capacity(2).floor(2).property(Property.builder().propertyId("HOT-1").propertyName("Hotel").type(1).address("A").province(1).activeStatus(1).totalRoom(0).ownerName("O").ownerId(UUID.randomUUID()).build()).build();
        Room room = Room.builder().roomId("RM-1").name("101").roomType(rt).availabilityStatus(1).activeRoom(1).build();
        entity.setRoom(room);

        AccommodationBookingDto dto = AccommodationBookingMapper.toDto(entity);
        assertEquals("BK-1", dto.getBookingId());
        assertEquals("101", dto.getRoomName());
        assertEquals("Deluxe", dto.getRoomTypeName());
        assertEquals("Hotel", dto.getPropertyName());
        assertEquals("+62-81234567890", dto.getCustomerPhone()); // normalization applied
    }
}
