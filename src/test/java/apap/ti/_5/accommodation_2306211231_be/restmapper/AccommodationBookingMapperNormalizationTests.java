package apap.ti._5.accommodation_2306211231_be.restmapper;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import apap.ti._5.accommodation_2306211231_be.models.AccommodationBooking;

class AccommodationBookingMapperNormalizationTests {

    @Test
    void toDto_nullExtraAndRefund_normalizedToZero() {
        AccommodationBooking b = new AccommodationBooking();
        b.setExtraPay(null);
        b.setRefund(null);
        var dto = AccommodationBookingMapper.toDto(b);
        assertNotNull(dto);
        assertEquals(0, dto.getExtraPay());
        assertEquals(0, dto.getRefund());
    }
}
