package apap.ti._5.accommodation_2306211231_be.restcontroller;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import apap.ti._5.accommodation_2306211231_be.restservice.PropertyRestService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PropertyRestControllerPredictTests {

    @Mock
    private PropertyRestService propertyRestService;

    @Test
    void predictReturnsNextSequenceAndPredictedId() throws Exception {
        var mockedAuth = mock(apap.ti._5.accommodation_2306211231_be.restservice.AuthRestService.class);
        var mockedReview = mock(apap.ti._5.accommodation_2306211231_be.restservice.AccommodationReviewRestService.class);
        PropertyRestController controller = new PropertyRestController(propertyRestService, mockedAuth, mockedReview);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();

        when(propertyRestService.predictNextPropertySequence()).thenReturn(5);
        UUID owner = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        mvc.perform(get("/property/predict")
                .param("type", "1")
                .param("ownerId", owner.toString())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status", is(200)))
            .andExpect(jsonPath("$.data.nextSequence", is(5)))
            .andExpect(jsonPath("$.data.predictedPropertyId", containsString("HOT-")));

        verify(propertyRestService, times(1)).predictNextPropertySequence();
    }
}
