package apap.ti._5.accommodation_2306211231_be.restcontroller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import jakarta.persistence.EntityManagerFactory;

import apap.ti._5.accommodation_2306211231_be.restservice.AccommodationBookingRestService;
import apap.ti._5.accommodation_2306211231_be.restservice.AuthRestService;
import apap.ti._5.accommodation_2306211231_be.restservice.AccommodationReviewRestService;
import apap.ti._5.accommodation_2306211231_be.restservice.CustomerRestService;
import apap.ti._5.accommodation_2306211231_be.security.jwt.JwtUtils;

@WebMvcTest(controllers = AccommodationBookingRestController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class, DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
class AccommodationBookingRestControllerErrorIT {

    @Autowired MockMvc mockMvc;
    @MockBean AccommodationBookingRestService service;
    @MockBean AuthRestService authRestService;
    @MockBean AccommodationReviewRestService reviewService;
    @MockBean CustomerRestService customerService;
    @MockBean JwtUtils jwtUtils;
    @MockBean UserDetailsService userDetailsService;
    @MockBean EntityManagerFactory entityManagerFactory;

    @Test
    void getBooking_notFound_returns404() throws Exception {
        when(service.getBookingDtoById(anyString())).thenThrow(new NoSuchElementException("not found"));
        mockMvc.perform(get("/bookings/UNKNOWN").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void markPaid_error_returns500Wrapper() throws Exception {
        when(service.markBookingAsPaid(anyString())).thenThrow(new RuntimeException("boom"));
        String body = "{\"data\":{\"bookingId\":\"BK-ERR\"}}";
        mockMvc.perform(post("/bookings/status/pay").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.status").value(500));
    }
}
