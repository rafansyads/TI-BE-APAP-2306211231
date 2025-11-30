package apap.ti._5.accommodation_2306211231_be.restmapper;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.response.accommodationbooking.AccommodationBookingDto;

class AccommodationBookingMapperNullTests {

    @Test
    void toDtoNullReturnsNull() {
        assertNull(AccommodationBookingMapper.toDto(null));
    }

    @Test
    void fromCreateRequestNullReturnsNull() {
        assertNull(AccommodationBookingMapper.fromCreateRequest((AccommodationBookingCreateRequest) null));
    }

    @Test
    void updateEntityNullSafe() {
        // Should simply return without throwing when entity or request null
        AccommodationBookingMapper.updateEntity(null, null);
    }
}
