package apap.ti._5.accommodation_2306211231_be.restcontroller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import apap.ti._5.accommodation_2306211231_be.restservice.AccommodationBookingRestService;
import apap.ti._5.accommodation_2306211231_be.restservice.PropertyRestService;
import apap.ti._5.accommodation_2306211231_be.service.ProvinceService;

class BaseAccommodationRestControllerErrorTests {

    private MockMvc mvc;
    private PropertyRestService propertyService;
    private AccommodationBookingRestService bookingService;
    private ProvinceService provinceService;

    @BeforeEach
    void setup() {
        propertyService = mock(PropertyRestService.class);
        bookingService = mock(AccommodationBookingRestService.class);
        provinceService = mock(ProvinceService.class);
        when(propertyService.count()).thenThrow(new RuntimeException("boom"));
        var controller = new BaseAccommodationRestController(propertyService, bookingService, provinceService);
        mvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void homeError_returns500Wrapped() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/"))
           .andExpect(status().isInternalServerError())
           .andExpect(jsonPath("$.status").value(500))
           .andExpect(jsonPath("$.message").isNotEmpty());
    }
}
