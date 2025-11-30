package apap.ti._5.accommodation_2306211231_be.restmapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import apap.ti._5.accommodation_2306211231_be.models.AccommodationReview;
import apap.ti._5.accommodation_2306211231_be.models.Property;
import apap.ti._5.accommodation_2306211231_be.models.profile.Customer;
import apap.ti._5.accommodation_2306211231_be.restdto.response.accommodationbooking.AccommodationReviewDTO;

class AccommodationReviewMapperTest {

    @Test
    void toDTO_mapsFields_and_handlesNullBooking() {
        AccommodationReview r = new AccommodationReview();
        r.setReviewId("rev-1");
        Property p = new Property();
        p.setPropertyId("prop-1");
        r.setProperty(p);
        Customer c = new Customer();
        c.setUsername("custx");
        r.setCustomer(c);
        r.setOverallRating(4);
        r.setCreatedDate(LocalDateTime.now());

        AccommodationReviewDTO dto = AccommodationReviewMapper.toDTO(r);
        assertNotNull(dto);
        assertEquals("rev-1", dto.getReviewId());
        assertEquals("prop-1", dto.getPropertyId());
        assertEquals("custx", dto.getCustomerUsername());
        assertEquals(4, dto.getOverallRating());
    }
}
