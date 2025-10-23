package apap.ti._5.accommodation_2306211231_be.restmapper;

import apap.ti._5.accommodation_2306211231_be.models.AccommodationBooking;
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingUpdateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.response.accommodationbooking.AccommodationBookingDto;

import java.time.LocalDateTime;
import java.util.UUID;

public final class AccommodationBookingMapper {
    private AccommodationBookingMapper() {}

    public static AccommodationBookingDto toDto(AccommodationBooking b) {
        if (b == null) return null;
        return new AccommodationBookingDto(
                b.getBookingId(),
                b.getCheckInDate() != null ? b.getCheckInDate().toString() : null,
                b.getCheckOutDate() != null ? b.getCheckOutDate().toString() : null,
                b.getTotalDays(),
                b.getTotalPrice(),
                b.getStatus(),
                b.getCustomerId() != null ? b.getCustomerId().toString() : null,
                b.getCustomerName(),
                b.getCustomerEmail(),
                b.getCustomerPhone(),
                b.getIsBreakfast(),
                b.getRefund(),
                b.getExtraPay(),
                b.getCapacity(),
                b.getRoom() != null ? b.getRoom().getRoomId() : null
        );
    }

    public static AccommodationBooking fromCreateRequest(AccommodationBookingCreateRequest req) {
        if (req == null) return null;
        AccommodationBooking b = new AccommodationBooking();
        b.setBookingId(req.getBookingId());
        b.setCheckInDate(parse(req.getCheckInDate()));
        b.setCheckOutDate(parse(req.getCheckOutDate()));
        b.setTotalDays(req.getTotalDays());
        b.setTotalPrice(req.getTotalPrice());
        b.setStatus(req.getStatus());
        b.setCustomerId(UUID.fromString(req.getCustomerId()));
        b.setCustomerName(req.getCustomerName());
        b.setCustomerEmail(req.getCustomerEmail());
        b.setCustomerPhone(req.getCustomerPhone());
        b.setIsBreakfast(req.getIsBreakfast());
        b.setRefund(req.getRefund());
        b.setExtraPay(req.getExtraPay());
        b.setCapacity(req.getCapacity());
        // room relation to be set in service
        return b;
    }

    public static void updateEntity(AccommodationBooking b, AccommodationBookingUpdateRequest req) {
        if (b == null || req == null) return;
        b.setCheckInDate(parse(req.getCheckInDate()));
        b.setCheckOutDate(parse(req.getCheckOutDate()));
        b.setTotalDays(req.getTotalDays());
        b.setTotalPrice(req.getTotalPrice());
        b.setStatus(req.getStatus());
        b.setCustomerName(req.getCustomerName());
        b.setCustomerEmail(req.getCustomerEmail());
        b.setCustomerPhone(req.getCustomerPhone());
        b.setIsBreakfast(req.getIsBreakfast());
        b.setRefund(req.getRefund());
        b.setExtraPay(req.getExtraPay());
        b.setCapacity(req.getCapacity());
        // room relation can be changed in service using req.getRoomId()
    }

    private static LocalDateTime parse(String s) {
        if (s == null || s.isBlank()) return null;
        return LocalDateTime.parse(s);
    }
}
