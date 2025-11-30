package apap.ti._5.accommodation_2306211231_be.restservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

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
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingUpdateRequest;

@ExtendWith(MockitoExtension.class)
class AccommodationBookingRestServiceUpdatePositiveTests {

    @Mock AccommodationBookingRepository bookingRepository;
    @Mock RoomRepository roomRepository;
    @Mock PropertyRepository propertyRepository;
    @Mock apap.ti._5.accommodation_2306211231_be.restservice.CustomerRestService customerService;
    @InjectMocks AccommodationBookingRestService service;

    @Test
    void updatePaidBooking_longerStay_setsExtraPay_andRevertsToWaiting_andAdjustsProfit() {
    Property prop = Property.builder().propertyId("HOT-ABCD-001").propertyName("Hotel A").profit(100000).build();
        RoomType rt = RoomType.builder().roomTypeId("001-Deluxe-2").name("Deluxe").price(100000).capacity(2).property(prop).build();
        Room room = Room.builder().roomId("HOT-ABCD-001-201").name("201").roomType(rt).availabilityStatus(1).activeRoom(1).build();

        AccommodationBooking existing = new AccommodationBooking();
        existing.setBookingId("BOOK-1");
        existing.setRoom(room);
        existing.setCheckInDate(LocalDateTime.now().plusDays(5).withHour(14));
        existing.setCheckOutDate(LocalDateTime.now().plusDays(6).withHour(12));
    existing.setTotalDays(1);
    existing.setTotalPrice(100000); // 1 day base price only
        existing.setStatus(1); // paid
        existing.setCustomerId(UUID.randomUUID());
        existing.setCustomerName("Alice");
        existing.setCustomerEmail("alice@mail.com");
    existing.setCustomerPhone("08123456789");
    existing.setIsBreakfast(Boolean.FALSE);

        when(bookingRepository.findById("BOOK-1")).thenReturn(Optional.of(existing));
        when(roomRepository.findById(room.getRoomId())).thenReturn(Optional.of(room));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(propertyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AccommodationBookingUpdateRequest req = new AccommodationBookingUpdateRequest();
        req.setBookingId("BOOK-1");
        req.setRoomId(room.getRoomId());
        req.setRoomName("201");
        req.setRoomTypeName("Deluxe");
        req.setPropertyName("Hotel A");
    // keep same dates but add breakfast to cause positive delta
    req.setCheckInDate(existing.getCheckInDate());
    req.setCheckOutDate(existing.getCheckOutDate());
    req.setTotalDays(1);
    req.setTotalPrice(100000);
        req.setStatus(1);
        req.setCustomerId(existing.getCustomerId().toString());
        req.setCustomerName("Alice");
        req.setCustomerEmail("alice@mail.com");
    req.setCustomerPhone("08123456789");
    req.setIsBreakfast(Boolean.TRUE); // add breakfast
        req.setRefund(0);
        req.setExtraPay(0);
        req.setCapacity(2);

        var dto = service.updateBooking("BOOK-1", req);
        assertEquals(0, dto.getStatus()); // reverted to waiting
    assertTrue(dto.getExtraPay() > 0);
    // Profit adjusted down from 100000 to 0
        assertEquals(0, prop.getProfit());
    }
}
