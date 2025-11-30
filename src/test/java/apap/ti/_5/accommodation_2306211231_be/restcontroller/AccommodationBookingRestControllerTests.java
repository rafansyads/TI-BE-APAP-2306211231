package apap.ti._5.accommodation_2306211231_be.restcontroller;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import apap.ti._5.accommodation_2306211231_be.restdto.BaseRequestDto;
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingCancelRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingPayRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingRefundRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.response.accommodationbooking.AccommodationBookingDto;
import apap.ti._5.accommodation_2306211231_be.restservice.AccommodationBookingRestService;

@ExtendWith(MockitoExtension.class)
class AccommodationBookingRestControllerTests {

    @Mock private AccommodationBookingRestService bookingService;
    @InjectMocks private AccommodationBookingRestController controller;

    private MockMvc mvc;
    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void payEndpointReturns200() throws Exception {
        AccommodationBookingDto dto = new AccommodationBookingDto();
        dto.setBookingId("BOOK-001"); dto.setStatus(1);
        when(bookingService.markBookingAsPaid("BOOK-001")).thenReturn(dto);

        var reqData = new AccommodationBookingPayRequest(); reqData.setBookingId("BOOK-001");
        BaseRequestDto<AccommodationBookingPayRequest> req = new BaseRequestDto<>(); req.setData(reqData);

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/bookings/status/pay")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(req)))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.status", is(1)));
    }

    @Test
    void cancelEndpointReturns200() throws Exception {
        AccommodationBookingDto dto = new AccommodationBookingDto();
        dto.setBookingId("BOOK-002"); dto.setStatus(2);
        when(bookingService.cancelBooking("BOOK-002")).thenReturn(dto);

        var reqData = new AccommodationBookingCancelRequest(); reqData.setBookingId("BOOK-002");
        BaseRequestDto<AccommodationBookingCancelRequest> req = new BaseRequestDto<>(); req.setData(reqData);

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/bookings/status/cancel")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(req)))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.status", is(2)));
    }

    @Test
    void refundEndpointReturns200() throws Exception {
        AccommodationBookingDto dto = new AccommodationBookingDto();
        dto.setBookingId("BOOK-003"); dto.setStatus(3);
        when(bookingService.refundBooking("BOOK-003")).thenReturn(dto);

        var reqData = new AccommodationBookingRefundRequest(); reqData.setBookingId("BOOK-003");
        BaseRequestDto<AccommodationBookingRefundRequest> req = new BaseRequestDto<>(); req.setData(reqData);

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/bookings/status/refund")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(req)))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.status", is(3)));
    }

    @Test
    void chartEndpointReturns200WithLabelsAndData() throws Exception {
        Map<String, Object> chart = new HashMap<>();
        chart.put("labels", java.util.List.of("King The Land"));
        chart.put("data", java.util.List.of(123));
        when(bookingService.getBookingChart(10, 2025, null)).thenReturn(chart);

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/bookings/chart")
            .param("month", "10").param("year", "2025"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.labels[0]", is("King The Land")))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.data[0]", is(123)));
    }
}
