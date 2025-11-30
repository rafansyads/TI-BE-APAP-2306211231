package apap.ti._5.accommodation_2306211231_be.restmapper;

import apap.ti._5.accommodation_2306211231_be.models.AccommodationBooking;
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingUpdateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.response.accommodationbooking.AccommodationBookingDto;
import apap.ti._5.accommodation_2306211231_be.util.PhoneUtil;

import java.util.UUID;

public final class AccommodationBookingMapper {
    private AccommodationBookingMapper() {}

    public static AccommodationBookingDto toDto(AccommodationBooking b) {
        if (b == null) return null;
        Integer roomTypePrice = null;
        String propertyName = null;
        String roomTypeName = null;
        String roomName = null;
        // Normalize monetary nullable fields to 0 for API consistency
        Integer refund = b.getRefund() == null ? 0 : b.getRefund();
        Integer extraPay = b.getExtraPay() == null ? 0 : b.getExtraPay();
        if (b.getRoom() != null) {
            roomName = b.getRoom().getName();
            if (b.getRoom().getRoomType() != null) {
                roomTypeName = b.getRoom().getRoomType().getName();
                roomTypePrice = b.getRoom().getRoomType().getPrice();
                if (b.getRoom().getRoomType().getProperty() != null) {
                    propertyName = b.getRoom().getRoomType().getProperty().getPropertyName();
                }
            }
        }
        return new AccommodationBookingDto(
                b.getBookingId(),
                b.getCheckInDate(),
                b.getCheckOutDate(),
                b.getTotalDays(),
                b.getTotalPrice(),
                b.getStatus(),
                b.getCustomerId() != null ? b.getCustomerId().toString() : null,
                b.getCustomerName(),
                b.getCustomerEmail(),
                b.getCustomerPhone(),
                b.getIsBreakfast(),
                refund,
                extraPay,
                b.getCapacity(),
                b.getRoom() != null ? b.getRoom().getRoomId() : null,
                propertyName,
                roomTypeName,
                roomName,
        roomTypePrice,
        b.getCreatedDate(),
        b.getUpdatedDate()
        );
    }

    public static AccommodationBooking fromCreateRequest(AccommodationBookingCreateRequest req) {
        if (req == null) return null;
        AccommodationBooking b = new AccommodationBooking();
        b.setBookingId(req.getBookingId());
        b.setCheckInDate(req.getCheckInDate());
        b.setCheckOutDate(req.getCheckOutDate());
        b.setTotalDays(req.getTotalDays());
        b.setTotalPrice(req.getTotalPrice());
        b.setStatus(req.getStatus());
        b.setCustomerId(parseUuidOrThrow(req.getCustomerId()));
        b.setCustomerName(req.getCustomerName());
        b.setCustomerEmail(req.getCustomerEmail());
        b.setCustomerPhone(PhoneUtil.normalizeOrThrow(req.getCustomerPhone()));
        b.setIsBreakfast(req.getIsBreakfast());
        b.setRefund(req.getRefund());
        b.setExtraPay(req.getExtraPay());
        b.setCapacity(req.getCapacity());
        // room relation to be set in service
        return b;
    }

    public static void updateEntity(AccommodationBooking b, AccommodationBookingUpdateRequest req) {
        if (b == null || req == null) return;
        b.setCheckInDate(req.getCheckInDate());
        b.setCheckOutDate(req.getCheckOutDate());
        b.setTotalDays(req.getTotalDays());
        b.setTotalPrice(req.getTotalPrice());
        b.setStatus(req.getStatus());
        b.setCustomerName(req.getCustomerName());
        b.setCustomerEmail(req.getCustomerEmail());
        b.setCustomerPhone(PhoneUtil.normalizeOrThrow(req.getCustomerPhone()));
        b.setIsBreakfast(req.getIsBreakfast());
        b.setRefund(req.getRefund());
        b.setExtraPay(req.getExtraPay());
        b.setCapacity(req.getCapacity());
        // room relation can be changed in service using req.getRoomId()
    }

    private static UUID parseUuidOrThrow(String raw) {
        if (raw == null) throw new IllegalArgumentException("customerId is required");
        String s = raw.trim();
        try { return UUID.fromString(s); }
        catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid customerId UUID format", ex);
        }
    }
}
