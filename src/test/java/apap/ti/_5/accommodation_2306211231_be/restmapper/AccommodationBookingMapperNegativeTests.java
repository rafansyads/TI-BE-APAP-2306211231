package apap.ti._5.accommodation_2306211231_be.restmapper;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingUpdateRequest;

class AccommodationBookingMapperNegativeTests {

    @Test
    void fromCreateRequest_invalidCustomerId_throws() {
        AccommodationBookingCreateRequest req = new AccommodationBookingCreateRequest();
        req.setCustomerId("not-a-uuid");
        req.setCustomerPhone("081234567890");
        assertThrows(IllegalArgumentException.class, () -> AccommodationBookingMapper.fromCreateRequest(req));
    }

    @Test
    void updateEntity_invalidPhone_throws() {
        var entity = new apap.ti._5.accommodation_2306211231_be.models.AccommodationBooking();
        var req = new AccommodationBookingUpdateRequest();
        req.setCustomerPhone("123"); // invalid phone
        assertThrows(IllegalArgumentException.class, () -> AccommodationBookingMapper.updateEntity(entity, req));
    }
}
