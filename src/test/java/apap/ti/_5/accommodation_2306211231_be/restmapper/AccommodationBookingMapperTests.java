package apap.ti._5.accommodation_2306211231_be.restmapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import apap.ti._5.accommodation_2306211231_be.models.AccommodationBooking;
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.response.accommodationbooking.AccommodationBookingDto;

class AccommodationBookingMapperTests {

    @Test
    void fromCreateRequestParsesDatesAndUuid() {
        var req = new AccommodationBookingCreateRequest();
    req.setCheckInDate(LocalDateTime.now().plusDays(1).withHour(14).withMinute(0).withSecond(0).withNano(0));
    req.setCheckOutDate(LocalDateTime.now().plusDays(2).withHour(12).withMinute(0).withSecond(0).withNano(0));
        req.setTotalDays(1); req.setTotalPrice(100); req.setStatus(0);
        req.setCustomerId(UUID.randomUUID().toString());
    req.setCustomerName("A"); req.setCustomerEmail("a@a.com"); req.setCustomerPhone("081234567890");
        req.setIsBreakfast(Boolean.TRUE); req.setRefund(0); req.setExtraPay(0); req.setCapacity(1); req.setRoomId("R");

        AccommodationBooking b = AccommodationBookingMapper.fromCreateRequest(req);
        assertNotNull(b.getCheckInDate());
        assertNotNull(b.getCheckOutDate());
        assertNotNull(b.getCustomerId());
        assertEquals(1, b.getTotalDays());
    }

    @Test
    void toDtoCopiesCoreFields() {
        var b = AccommodationBooking.builder()
                .bookingId("BOOK-001")
                .checkInDate(LocalDateTime.now())
                .checkOutDate(LocalDateTime.now().plusDays(1))
                .totalDays(1)
                .totalPrice(100)
                .status(0)
                .customerId(UUID.randomUUID())
                .customerName("A")
                .customerEmail("a@a.com")
                .customerPhone("08")
                .isBreakfast(Boolean.FALSE)
                .refund(0)
                .extraPay(0)
                .capacity(1)
                .build();

        AccommodationBookingDto dto = AccommodationBookingMapper.toDto(b);
        assertEquals("BOOK-001", dto.getBookingId());
        assertEquals(1, dto.getTotalDays());
        assertEquals(0, dto.getStatus());
    }
}
