package apap.ti._5.accommodation_2306211231_be.restservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
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
class AccommodationBookingRestServiceStatusMatrixTests {

    @Mock AccommodationBookingRepository bookingRepository;
    @Mock RoomRepository roomRepository;
    @Mock PropertyRepository propertyRepository;
    @Mock apap.ti._5.accommodation_2306211231_be.restservice.CustomerRestService customerService;
    @InjectMocks AccommodationBookingRestService service;

    private static class Chain {
        Property prop; RoomType rt; Room room; AccommodationBooking booking;
    }

    private Chain baseChain(int price, int status, int totalPrice, boolean breakfast) {
        Chain c = new Chain();
        c.prop = Property.builder().propertyId("HOT-MATRIX-1").propertyName("Hotel M").type(1)
            .address("Addr").province(1).activeStatus(1).totalRoom(0).ownerName("Owner").ownerId(UUID.randomUUID()).profit(totalPrice).build();
        c.rt = RoomType.builder().roomTypeId("RT-M").name("Deluxe").price(price).capacity(2).floor(1).property(c.prop).build();
        c.room = Room.builder().roomId("RM-M-101").name("101").roomType(c.rt).availabilityStatus(1).activeRoom(1).build();
        c.booking = new AccommodationBooking();
        c.booking.setBookingId("BK-M-1");
        c.booking.setRoom(c.room);
        c.booking.setStatus(status);
        c.booking.setTotalPrice(totalPrice);
        c.booking.setCheckInDate(LocalDateTime.now().plusDays(3).withHour(14));
        c.booking.setCheckOutDate(LocalDateTime.now().plusDays(5).withHour(12)); // 2 days baseline
        c.booking.setIsBreakfast(breakfast);
        c.booking.setCustomerId(UUID.randomUUID());
        return c;
    }

    @Test
    void update_waitingStatus_computesExtraOrRefundAndStaysWaiting() {
        Chain c = baseChain(100_000, 0, 200_000, false); // 2 nights * 100k
        when(bookingRepository.findById("BK-M-1")).thenReturn(Optional.of(c.booking));
        when(roomRepository.findById(c.room.getRoomId())).thenReturn(Optional.of(c.room));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AccommodationBookingUpdateRequest longer = new AccommodationBookingUpdateRequest();
        longer.setBookingId("BK-M-1");
        longer.setRoomId(c.room.getRoomId());
        longer.setRoomName(c.room.getName());
        longer.setRoomTypeName(c.rt.getName());
        longer.setPropertyName(c.prop.getPropertyName());
        longer.setCheckInDate(c.booking.getCheckInDate());
        longer.setCheckOutDate(c.booking.getCheckInDate().plusDays(3)); // 3 nights -> +100k
        longer.setTotalPrice(c.booking.getTotalPrice());
        longer.setTotalDays(3);
        longer.setStatus(0);
        longer.setCustomerId(c.booking.getCustomerId().toString());
        longer.setCustomerName("A"); longer.setCustomerEmail("a@a.com"); longer.setCustomerPhone("08123456789");
        longer.setIsBreakfast(false); longer.setCapacity(2);

        var dtoLonger = service.updateBooking("BK-M-1", longer);
        assertEquals(0, dtoLonger.getStatus());
        assertEquals(100_000, dtoLonger.getExtraPay());
        assertEquals(0, dtoLonger.getRefund());

        // Shorter -> refund suggested
        c.booking.setExtraPay(0); c.booking.setRefund(0); // reset
        when(bookingRepository.findById("BK-M-1")).thenReturn(Optional.of(c.booking));

        AccommodationBookingUpdateRequest shorter = new AccommodationBookingUpdateRequest();
        shorter.setBookingId("BK-M-1");
        shorter.setRoomId(c.room.getRoomId());
        shorter.setRoomName(c.room.getName());
        shorter.setRoomTypeName(c.rt.getName());
        shorter.setPropertyName(c.prop.getPropertyName());
        shorter.setCheckInDate(c.booking.getCheckInDate());
        shorter.setCheckOutDate(c.booking.getCheckInDate().plusDays(1)); // 1 night -> -100k -> refund
        shorter.setTotalPrice(c.booking.getTotalPrice());
        shorter.setTotalDays(1);
        shorter.setStatus(0);
        shorter.setCustomerId(c.booking.getCustomerId().toString());
        shorter.setCustomerName("A"); shorter.setCustomerEmail("a@a.com"); shorter.setCustomerPhone("08123456789");
        shorter.setIsBreakfast(false); shorter.setCapacity(2);

        var dtoShorter = service.updateBooking("BK-M-1", shorter);
        assertEquals(0, dtoShorter.getStatus());
        assertEquals(100_000, dtoShorter.getRefund());
        assertEquals(0, dtoShorter.getExtraPay());
    }

    @Test
    void update_paidStatus_longerStay_requiresExtra_andRevertsToWaiting_andRollsBackProfit() {
        Chain c = baseChain(100_000, 1, 200_000, false); // already paid, profit recognized 200k
        when(bookingRepository.findById("BK-M-1")).thenReturn(Optional.of(c.booking));
        when(roomRepository.findById(c.room.getRoomId())).thenReturn(Optional.of(c.room));
        when(propertyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AccommodationBookingUpdateRequest upd = new AccommodationBookingUpdateRequest();
        upd.setBookingId("BK-M-1");
        upd.setRoomId(c.room.getRoomId());
        upd.setRoomName(c.room.getName());
        upd.setRoomTypeName(c.rt.getName());
        upd.setPropertyName(c.prop.getPropertyName());
        upd.setCheckInDate(c.booking.getCheckInDate());
        upd.setCheckOutDate(c.booking.getCheckInDate().plusDays(3)); // 3 nights -> +100k
        upd.setTotalPrice(c.booking.getTotalPrice());
        upd.setStatus(1);
        upd.setTotalDays(3);
        upd.setCustomerId(c.booking.getCustomerId().toString());
        upd.setCustomerName("A"); upd.setCustomerEmail("a@a.com"); upd.setCustomerPhone("08123456789");
        upd.setIsBreakfast(false); upd.setCapacity(2);

        var dto = service.updateBooking("BK-M-1", upd);
        assertEquals(0, dto.getStatus()); // reverted to waiting
        assertEquals(100_000, dto.getExtraPay());
        assertEquals(0, dto.getRefund());
        // profit rolled back by previous total
        assertEquals(0, c.prop.getProfit());
    }

    @Test
    void update_paidStatus_shorterStay_setsRefund_andStatusRefundRequested() {
        Chain c = baseChain(100_000, 1, 200_000, false);
        when(bookingRepository.findById("BK-M-1")).thenReturn(Optional.of(c.booking));
        when(roomRepository.findById(c.room.getRoomId())).thenReturn(Optional.of(c.room));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AccommodationBookingUpdateRequest upd = new AccommodationBookingUpdateRequest();
        upd.setBookingId("BK-M-1");
        upd.setRoomId(c.room.getRoomId());
        upd.setRoomName(c.room.getName());
        upd.setRoomTypeName(c.rt.getName());
        upd.setPropertyName(c.prop.getPropertyName());
        upd.setCheckInDate(c.booking.getCheckInDate());
        upd.setCheckOutDate(c.booking.getCheckInDate().plusDays(1)); // 1 night -> -100k
        upd.setTotalPrice(c.booking.getTotalPrice());
        upd.setStatus(1);
        upd.setTotalDays(1);
        upd.setCustomerId(c.booking.getCustomerId().toString());
        upd.setCustomerName("A"); upd.setCustomerEmail("a@a.com"); upd.setCustomerPhone("08123456789");
        upd.setIsBreakfast(false); upd.setCapacity(2);

        var dto = service.updateBooking("BK-M-1", upd);
        assertEquals(3, dto.getStatus());
        assertEquals(100_000, dto.getRefund());
        assertEquals(0, dto.getExtraPay());
        // profit not changed yet on update
        assertEquals(200_000, c.prop.getProfit());
    }

    @Test
    void processCheckInToday_marksPaidToDone() {
        Chain c = baseChain(100_000, 1, 200_000, false);
        // Ensure check-in is today at or after normalized anchor
        LocalDateTime today14 = LocalDate.now().atTime(14, 0);
        c.booking.setCheckInDate(today14.plusHours(1));
        when(bookingRepository.findAll()).thenReturn(java.util.List.of(c.booking));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        int changed = service.processCheckInToday();
        assertEquals(1, changed);
        assertEquals(4, c.booking.getStatus());
    }
}
