package apap.ti._5.accommodation_2306211231_be.restcontroller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import apap.ti._5.accommodation_2306211231_be.restservice.PropertyRestService;
import apap.ti._5.accommodation_2306211231_be.restservice.AuthRestService;
import apap.ti._5.accommodation_2306211231_be.restservice.AccommodationReviewRestService;

@WebMvcTest(controllers = PropertyRestController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PropertyRestControllerMvcTests {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PropertyRestService propertyService;

    @MockBean
    private AuthRestService authRestService;

    @MockBean
    private AccommodationReviewRestService reviewService;

    // other common beans required by slice
    @MockBean
    private apap.ti._5.accommodation_2306211231_be.security.jwt.JwtUtils jwtUtils;
    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    // EntityManagerFactory not required for this WebMvc slice test; remove to avoid javax.persistence

    @Test
    void listProperties_empty_returnsOkWithEmptyList() throws Exception {
        when(propertyService.getAllPropertiesDto()).thenReturn(new ArrayList<>());

        mvc.perform(get("/property"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
}
