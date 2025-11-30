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
class AccommodationBookingRestServicePaidPositiveDeltaTests {

    @Mock AccommodationBookingRepository bookingRepository;
    @Mock RoomRepository roomRepository;
    @Mock PropertyRepository propertyRepository;
    @Mock apap.ti._5.accommodation_2306211231_be.restservice.CustomerRestService customerService;
    @InjectMocks AccommodationBookingRestService service;

    @Test
    void paidBooking_priceIncrease_onlyPriceChange_setsExtra_andRevertsToWaiting() {
        // Existing booking: paid (1), baseline 2 nights, price 100k -> total 200k
        Property prop = Property.builder().propertyId("HOT-T").propertyName("Hotel T").profit(200_000).build();
        RoomType rt = RoomType.builder().roomTypeId("RT-T").name("Deluxe").price(100_000).capacity(2).floor(1).property(prop).build();
        Room room = Room.builder().roomId("RM-T-101").name("101").roomType(rt).availabilityStatus(1).activeRoom(1).build();
        AccommodationBooking existing = new AccommodationBooking();
        existing.setBookingId("BK-T-1");
        existing.setRoom(room);
        existing.setStatus(1);
        existing.setTotalPrice(200_000);
        existing.setCheckInDate(LocalDateTime.now().plusDays(3).withHour(14));
        existing.setCheckOutDate(LocalDateTime.now().plusDays(5).withHour(12)); // 2 nights
        existing.setIsBreakfast(false);
        existing.setCustomerId(UUID.randomUUID());

    // We'll simulate a positive delta via breakfast change (dates unchanged)
    // Keep price 100k; turning on breakfast adds 50k/day -> delta = 100k for 2 nights

        when(bookingRepository.findById("BK-T-1")).thenReturn(Optional.of(existing));
        when(roomRepository.findById(room.getRoomId())).thenReturn(Optional.of(room));
        when(propertyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AccommodationBookingUpdateRequest req = new AccommodationBookingUpdateRequest();
        req.setBookingId("BK-T-1");
        req.setRoomId(room.getRoomId());
        req.setRoomName(room.getName());
        req.setRoomTypeName(rt.getName());
        req.setPropertyName(prop.getPropertyName());
    // Keep same dates; change breakfast to true to trigger positive delta
        req.setCheckInDate(existing.getCheckInDate());
        req.setCheckOutDate(existing.getCheckOutDate());
    req.setIsBreakfast(true);
        req.setCapacity(2);
        req.setCustomerId(existing.getCustomerId().toString());
    req.setCustomerName("Tono");
    req.setCustomerEmail("tono@example.com");
    req.setCustomerPhone("081234567890");

        var dto = service.updateBooking("BK-T-1", req);

    // Expect revert to waiting with extraPay 100k, and previous profit rolled back to 0
        assertEquals(0, dto.getStatus());
    assertEquals(100_000, dto.getExtraPay());
        assertEquals(0, dto.getRefund());
        assertEquals(0, prop.getProfit());
        // totalPrice baseline unchanged until payment
        assertEquals(200_000, dto.getTotalPrice());
    }
}
