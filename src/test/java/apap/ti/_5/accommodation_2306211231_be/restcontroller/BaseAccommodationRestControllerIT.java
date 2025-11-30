package apap.ti._5.accommodation_2306211231_be.restcontroller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import jakarta.persistence.EntityManagerFactory;

import apap.ti._5.accommodation_2306211231_be.restservice.AccommodationBookingRestService;
import apap.ti._5.accommodation_2306211231_be.restservice.PropertyRestService;
import apap.ti._5.accommodation_2306211231_be.service.ProvinceService;

@WebMvcTest(controllers = BaseAccommodationRestController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class, DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
class BaseAccommodationRestControllerIT {

    @Autowired MockMvc mockMvc;
    @MockBean PropertyRestService propertyService;
    @MockBean AccommodationBookingRestService bookingService;
    @MockBean ProvinceService provinceService;
    @MockBean UserDetailsService userDetailsService;
    @MockBean EntityManagerFactory entityManagerFactory;
    @MockBean apap.ti._5.accommodation_2306211231_be.security.jwt.JwtUtils jwtUtils;

    @Test
    void home_returnsCounts() throws Exception {
        when(propertyService.count()).thenReturn(5L);
        when(bookingService.count()).thenReturn(9L);
        mockMvc.perform(get("/").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalProperties").value(5))
            .andExpect(jsonPath("$.data.totalBookings").value(9));
    }

    @Test
    void provinces_returnsMap() throws Exception {
        when(provinceService.getAll()).thenReturn(Map.of(31, "DKI Jakarta"));
        mockMvc.perform(get("/external/province").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data['31']").value("DKI Jakarta"));
    }
}
