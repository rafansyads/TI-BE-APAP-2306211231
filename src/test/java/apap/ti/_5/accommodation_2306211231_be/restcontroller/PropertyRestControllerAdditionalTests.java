package apap.ti._5.accommodation_2306211231_be.restcontroller;

import apap.ti._5.accommodation_2306211231_be.restdto.response.property.PropertySummaryDto;
import apap.ti._5.accommodation_2306211231_be.restservice.PropertyRestService;
import apap.ti._5.accommodation_2306211231_be.restservice.AuthRestService;
import apap.ti._5.accommodation_2306211231_be.restservice.AccommodationReviewRestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import apap.ti._5.accommodation_2306211231_be.security.jwt.JwtUtils;
import org.springframework.security.core.userdetails.UserDetailsService;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PropertyRestController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class, DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class PropertyRestControllerAdditionalTests {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PropertyRestService propertyService;

    @MockBean
    private AuthRestService authRestService;

    @MockBean
    private AccommodationReviewRestService reviewService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private EntityManagerFactory entityManagerFactory;

    @Test
    void predictPropertyId_returnsPredicted() throws Exception {
        when(propertyService.predictNextPropertySequence()).thenReturn(7);
        String ownerId = UUID.randomUUID().toString();

        mvc.perform(get("/property/predict")
                .param("type", "1")
                .param("ownerId", ownerId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.nextSequence").value(7))
                .andExpect(jsonPath("$.data.predictedPropertyId").isNotEmpty());
    }

    @Test
    void listProperties_noAuth_returnsList() throws Exception {
        PropertySummaryDto dto = new PropertySummaryDto("PID-1", "Test Property", 1, 10, "Prov", 1, 2);
        when(propertyService.getAllPropertiesDto()).thenReturn(List.of(dto));

        mvc.perform(get("/property").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data[0].propertyId").value("PID-1"))
                .andExpect(jsonPath("$.data[0].propertyName").value("Test Property"));
    }
}
