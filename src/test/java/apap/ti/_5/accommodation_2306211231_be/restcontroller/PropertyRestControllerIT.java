package apap.ti._5.accommodation_2306211231_be.restcontroller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.*;

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

import apap.ti._5.accommodation_2306211231_be.restdto.response.property.PropertyDetailDto;
import apap.ti._5.accommodation_2306211231_be.restdto.response.property.PropertySummaryDto;
import apap.ti._5.accommodation_2306211231_be.restdto.response.property.OwnerSummaryDto;
import apap.ti._5.accommodation_2306211231_be.restservice.PropertyRestService;
import apap.ti._5.accommodation_2306211231_be.restservice.AuthRestService;
import apap.ti._5.accommodation_2306211231_be.restservice.AccommodationReviewRestService;
import apap.ti._5.accommodation_2306211231_be.security.jwt.JwtUtils;

@WebMvcTest(controllers = PropertyRestController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class, DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
class PropertyRestControllerIT {

    @Autowired MockMvc mockMvc;
    @MockBean PropertyRestService propertyService;
    @MockBean AuthRestService authRestService;
    @MockBean AccommodationReviewRestService reviewService;
    @MockBean JwtUtils jwtUtils;
    @MockBean UserDetailsService userDetailsService;
    @MockBean EntityManagerFactory entityManagerFactory;

    @Test
    void listProperties_empty_ok() throws Exception {
        when(propertyService.getAllPropertiesDto()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/property").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void predict_invalidUuid_returns400() throws Exception {
        mockMvc.perform(get("/property/predict").param("type","1").param("ownerId","bad-uuid"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getProperty_notFound_returns404() throws Exception {
        when(propertyService.getPropertyDetailDto(anyString())).thenThrow(new IllegalArgumentException("not found"));
        mockMvc.perform(get("/property/NOPE"))
            .andExpect(status().isNotFound());
    }

    @Test
    void owners_empty_ok() throws Exception {
        when(propertyService.getOwners()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/property/owners"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray());
    }
}
