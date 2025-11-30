package apap.ti._5.accommodation_2306211231_be.restcontroller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.ResponseEntity;

import apap.ti._5.accommodation_2306211231_be.restservice.AccommodationBookingRestService;
import apap.ti._5.accommodation_2306211231_be.restservice.AuthRestService;
import apap.ti._5.accommodation_2306211231_be.restservice.AccommodationReviewRestService;
import apap.ti._5.accommodation_2306211231_be.restservice.CustomerRestService;
import apap.ti._5.accommodation_2306211231_be.restdto.BaseResponseDto;
import apap.ti._5.accommodation_2306211231_be.restdto.response.accommodationbooking.AccommodationBookingDto;
import apap.ti._5.accommodation_2306211231_be.util.TestSecurityUtils;

import java.util.List;

class AccommodationBookingRestControllerSecurityTest {

    AccommodationBookingRestService bookingService;
    AuthRestService authRestService;
    AccommodationReviewRestService reviewService;
    CustomerRestService customerService;
    AccommodationBookingRestController controller;

    @BeforeEach
    void setup() {
        bookingService = mock(AccommodationBookingRestService.class);
        authRestService = mock(AuthRestService.class);
        reviewService = mock(AccommodationReviewRestService.class);
        customerService = mock(CustomerRestService.class);
        controller = new AccommodationBookingRestController(bookingService, authRestService, reviewService, customerService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listBookings_asSuperadmin_returnsOkAndEmptyListMessage() {
        Authentication auth = TestSecurityUtils.authWithRoles("super", "SUPERADMIN");
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(bookingService.getAllBookingsDto()).thenReturn(List.of());

        ResponseEntity<BaseResponseDto<java.util.ArrayList<AccommodationBookingDto>>> resp = controller.listBookings();
        assertNotNull(resp);
        assertEquals(200, resp.getStatusCodeValue());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().getMessage().contains("No bookings found") || resp.getBody().getMessage().contains("All bookings retrieved"));
        verify(bookingService, times(1)).getAllBookingsDto();
    }
}
