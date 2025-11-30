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
import apap.ti._5.accommodation_2306211231_be.repository.AccommodationBookingRepository;
import apap.ti._5.accommodation_2306211231_be.repository.PropertyRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomRepository;
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingUpdateRequest;

@ExtendWith(MockitoExtension.class)
class AccommodationBookingRestServiceRefundFlowTests {

    @Mock AccommodationBookingRepository bookingRepository;
    @Mock RoomRepository roomRepository;
    @Mock PropertyRepository propertyRepository;
    @Mock apap.ti._5.accommodation_2306211231_be.restservice.CustomerRestService customerService;
    @InjectMocks AccommodationBookingRestService service;

    @Test
    void updateThenRefundBooking_appliesProfitAndTotalReduction() {
        Property prop = Property.builder().propertyId("HOT-ABCD-001").propertyName("Hotel A").type(1)
                .address("A").province(1).activeStatus(1).totalRoom(0).ownerName("O").ownerId(UUID.randomUUID()).profit(150000).build();
        RoomType rt = RoomType.builder().roomTypeId("RT-1").name("Deluxe").price(100000).capacity(2).floor(2).property(prop).build();
        Room room = Room.builder().roomId("RM-1").name("101").roomType(rt).availabilityStatus(1).activeRoom(1).build();
        rt.setListRoom(java.util.List.of(room));
        prop.setListRoomType(java.util.List.of(rt));

        AccommodationBooking existing = new AccommodationBooking();
        existing.setBookingId("BOOK-1");
        existing.setRoom(room);
        existing.setCheckInDate(LocalDateTime.now().plusDays(2).withHour(14));
        existing.setCheckOutDate(LocalDateTime.now().plusDays(3).withHour(12));
        existing.setTotalDays(1);
        existing.setTotalPrice(150000); // 100k + 50k breakfast
        existing.setStatus(1); // paid
        existing.setIsBreakfast(Boolean.TRUE);
        existing.setCustomerId(UUID.randomUUID());

        when(bookingRepository.findById("BOOK-1")).thenReturn(Optional.of(existing));
        when(roomRepository.findById(room.getRoomId())).thenReturn(Optional.of(room));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(propertyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Update request: remove breakfast -> expected total decreases by 50k triggering refund state
        AccommodationBookingUpdateRequest req = new AccommodationBookingUpdateRequest();
        req.setBookingId("BOOK-1");
        req.setRoomId(room.getRoomId());
        req.setRoomName("101");
        req.setRoomTypeName("Deluxe");
        req.setPropertyName("Hotel A");
        req.setCheckInDate(existing.getCheckInDate());
        req.setCheckOutDate(existing.getCheckOutDate());
        req.setTotalDays(1);
        req.setTotalPrice(150000);
        req.setStatus(1);
        req.setCustomerId(existing.getCustomerId().toString());
        req.setCustomerName("Alice");
        req.setCustomerEmail("alice@mail.com");
        req.setCustomerPhone("+62-81234567890");
        req.setIsBreakfast(Boolean.FALSE);
        req.setCapacity(2);

        var dtoAfterUpdate = service.updateBooking("BOOK-1", req);
        assertEquals(3, dtoAfterUpdate.getStatus());
        assertEquals(50000, dtoAfterUpdate.getRefund());
        assertEquals(0, dtoAfterUpdate.getExtraPay());
        // Property profit unchanged until refund processed
        assertEquals(150000, prop.getProfit());

        // Process refund action
        existing.setRefund(dtoAfterUpdate.getRefund()); // mimic persisted state
        existing.setStatus(dtoAfterUpdate.getStatus());
        when(bookingRepository.findById("BOOK-1")).thenReturn(Optional.of(existing));

        var dtoAfterRefund = service.refundBooking("BOOK-1");
        assertEquals(1, dtoAfterRefund.getStatus());
        assertEquals(0, dtoAfterRefund.getRefund());
        assertEquals(100000, dtoAfterRefund.getTotalPrice());
        assertEquals(100000, prop.getProfit()); // profit reduced by 50k
    }
}
