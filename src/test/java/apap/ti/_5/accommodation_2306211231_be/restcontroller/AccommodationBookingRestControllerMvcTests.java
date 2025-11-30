package apap.ti._5.accommodation_2306211231_be.restcontroller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import apap.ti._5.accommodation_2306211231_be.restservice.AccommodationBookingRestService;
import apap.ti._5.accommodation_2306211231_be.restservice.AccommodationReviewRestService;
import apap.ti._5.accommodation_2306211231_be.restservice.CustomerRestService;
import apap.ti._5.accommodation_2306211231_be.restservice.AuthRestService;

@WebMvcTest(controllers = AccommodationBookingRestController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class, DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
class AccommodationBookingRestControllerMvcTests {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    AccommodationBookingRestService bookingService;

    @MockBean
    AuthRestService authRestService;

    @MockBean
    AccommodationReviewRestService reviewService;

    @MockBean
    CustomerRestService customerService;

    @MockBean
    UserDetailsService userDetailsService;

    @MockBean
    apap.ti._5.accommodation_2306211231_be.security.jwt.JwtUtils jwtUtils;

    @MockBean
    EntityManagerFactory entityManagerFactory;

    @Test
    void listBookings_noAuth_returnsForbidden() throws Exception {
        mockMvc.perform(get("/bookings").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void processCheckIn_unauthenticated_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/bookings/status/process-checkin").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void listReviews_missingCustomerId_returnsBadRequest() throws Exception {
        // send empty customerID param so controller logic returns BAD_REQUEST
        mockMvc.perform(get("/bookings/reviews").param("customerID", "").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }
}
