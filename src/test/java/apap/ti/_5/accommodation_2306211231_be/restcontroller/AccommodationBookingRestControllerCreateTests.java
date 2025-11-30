package apap.ti._5.accommodation_2306211231_be.restcontroller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

import apap.ti._5.accommodation_2306211231_be.restdto.response.accommodationbooking.AccommodationBookingDto;
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restservice.AccommodationBookingRestService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;

@WebMvcTest(controllers = AccommodationBookingRestController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class, DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
class AccommodationBookingRestControllerCreateTests {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    AccommodationBookingRestService bookingService;

    @MockBean
    apap.ti._5.accommodation_2306211231_be.restservice.AuthRestService authRestService;

    @MockBean
    apap.ti._5.accommodation_2306211231_be.restservice.AccommodationReviewRestService reviewService;

    @MockBean
    apap.ti._5.accommodation_2306211231_be.restservice.CustomerRestService customerService;

    @MockBean
    org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @MockBean
    apap.ti._5.accommodation_2306211231_be.security.jwt.JwtUtils jwtUtils;

    @MockBean
    EntityManagerFactory entityManagerFactory;

    @Test
    void createBooking_withData_returnsCreated() throws Exception {
        when(bookingService.createBooking(any(AccommodationBookingCreateRequest.class))).thenReturn(new AccommodationBookingDto());

        String body = "{\"data\":{}}"; // service is mocked to accept any
        mockMvc.perform(post("/bookings/create").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201));
    }

    @Test
    void createReview_missingBookingId_returnsBadRequest() throws Exception {
        String body = "{\"data\":{}}";
        // set an authenticated CUSTOMER in the security context so controller reaches validation
        var auth = new UsernamePasswordAuthenticationToken("customerUser", null,
            List.of(new SimpleGrantedAuthority("CUSTOMER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        mockMvc.perform(post("/bookings/reviews/create").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
        SecurityContextHolder.clearContext();
    }
}
