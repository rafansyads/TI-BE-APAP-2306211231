package apap.ti._5.accommodation_2306211231_be.restservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import apap.ti._5.accommodation_2306211231_be.models.*;
import apap.ti._5.accommodation_2306211231_be.repository.*;

@ExtendWith(MockitoExtension.class)
class AccommodationBookingRestServiceProcessCheckInEdgeTests {

    @Mock AccommodationBookingRepository bookingRepository;
    @Mock RoomRepository roomRepository;
    @Mock PropertyRepository propertyRepository;
    @Mock apap.ti._5.accommodation_2306211231_be.restservice.CustomerRestService customerService;
    @InjectMocks AccommodationBookingRestService service;

    private AccommodationBooking booking(int status, int total, int refund, Property p) {
        RoomType rt = RoomType.builder().roomTypeId("RT-X").name("Std").price(100000).capacity(2).floor(1).property(p).build();
        Room room = Room.builder().roomId("RM-X").name("101").roomType(rt).availabilityStatus(1).activeRoom(1).build();
        rt.setListRoom(List.of(room));
        var b = new AccommodationBooking();
        b.setBookingId(UUID.randomUUID().toString());
        b.setRoom(room);
        b.setCheckInDate(LocalDate.now().atTime(14,0));
        b.setCheckOutDate(LocalDate.now().plusDays(1).atTime(12,0));
        b.setStatus(status);
        b.setTotalPrice(total);
        b.setRefund(refund);
        return b;
    }

    @Test
    void processCheckIn_mixedStatuses_countsOnlyChanged() {
        Property prop = Property.builder().propertyId("HOT-PROC-1").propertyName("Hotel Z").type(1)
            .address("A").province(1).activeStatus(1).totalRoom(0).ownerName("O").ownerId(UUID.randomUUID()).profit(200000).build();
        var paid = booking(1, 150000, 0, prop); // should become done, profit unchanged
        var refundReq = booking(3, 200000, 50000, prop); // refund applied and status done, profit -50000
        var waitingExtra = booking(0, 0, 0, prop); waitingExtra.setExtraPay(100000); // unpaid with extraPay -> canceled
        var canceledAlready = booking(2, 100000, 0, prop); // status 2 remains unchanged

        List<AccommodationBooking> list = List.of(paid, refundReq, waitingExtra, canceledAlready);
        when(bookingRepository.findAll()).thenReturn(list);
        when(propertyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        int changed = service.processCheckInToday();
        assertEquals(3, changed); // paid, refundReq, waitingExtra changed; canceled stays
        assertEquals(4, paid.getStatus());
        assertEquals(4, refundReq.getStatus());
        assertEquals(2, waitingExtra.getStatus());
        assertEquals(2, canceledAlready.getStatus()); // unchanged
        // Profit: initial 200000 - refund 50000 = 150000 (paid booking already recognized earlier, canceled unpaid had no effect)
        assertEquals(150000, prop.getProfit());
        assertEquals(150000, refundReq.getTotalPrice()); // reduced by refund
        assertEquals(0, refundReq.getRefund());
    }
}
