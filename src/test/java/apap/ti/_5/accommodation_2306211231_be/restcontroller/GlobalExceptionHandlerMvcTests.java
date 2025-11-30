package apap.ti._5.accommodation_2306211231_be.restcontroller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.isEmptyString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import apap.ti._5.accommodation_2306211231_be.restdto.BaseRequestDto;
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingPayRequest;
import apap.ti._5.accommodation_2306211231_be.restservice.AccommodationBookingRestService;

/**
 * MVC test to verify GlobalExceptionHandler maps IllegalArgumentException to 400 response.
 */
class GlobalExceptionHandlerMvcTests {

    private MockMvc mvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        var mockedService = Mockito.mock(AccommodationBookingRestService.class);
        doThrow(new IllegalArgumentException("bad input")).when(mockedService).markBookingAsPaid(anyString());
        var mockedAuth = Mockito.mock(apap.ti._5.accommodation_2306211231_be.restservice.AuthRestService.class);
        var mockedReview = Mockito.mock(apap.ti._5.accommodation_2306211231_be.restservice.AccommodationReviewRestService.class);
        var mockedCust = Mockito.mock(apap.ti._5.accommodation_2306211231_be.restservice.CustomerRestService.class);
        var controller = new AccommodationBookingRestController(mockedService, mockedAuth, mockedReview, mockedCust);
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void illegalArgumentIsHandled_returns500_dueToControllerCatch() throws Exception {
        BaseRequestDto<AccommodationBookingPayRequest> req = new BaseRequestDto<>();
        var data = new AccommodationBookingPayRequest();
        data.setBookingId("ANY");
        req.setData(data);

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/bookings/status/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.status", is(500)))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.message", not(isEmptyString())));
    }
}
