package apap.ti._5.accommodation_2306211231_be.restcontroller;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import apap.ti._5.accommodation_2306211231_be.restservice.PropertyRestService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PropertyRestControllerErrorPathsTests {

    @Mock PropertyRestService propertyRestService;
    @InjectMocks PropertyRestController controller;
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void listProperties_whenServiceThrows_returns500() throws Exception {
        when(propertyRestService.getAllPropertiesDto()).thenThrow(new RuntimeException("boom"));
        mvc.perform(get("/property").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.status", is(500)))
            .andExpect(jsonPath("$.message", containsString("An error occurred while fetching properties")));
    }

    @Test
    void predict_withInvalidOwnerId_returns400() throws Exception {
        mvc.perform(get("/property/predict")
                .param("type", "1")
                .param("ownerId", "not-a-uuid"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status", is(400)))
            .andExpect(jsonPath("$.message", containsString("Invalid ownerId UUID")));
        verify(propertyRestService, never()).predictNextPropertySequence();
    }

    @Test
    void listOwners_whenServiceThrows_returns500() throws Exception {
        when(propertyRestService.getOwners()).thenThrow(new RuntimeException("db down"));
        mvc.perform(get("/property/owners").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.status", is(500)))
            .andExpect(jsonPath("$.message", containsString("Failed to fetch owners")));
    }
}
