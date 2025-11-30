package apap.ti._5.accommodation_2306211231_be.restcontroller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import apap.ti._5.accommodation_2306211231_be.restservice.AuthRestService;
import apap.ti._5.accommodation_2306211231_be.restservice.AccommodationReviewRestService;
import apap.ti._5.accommodation_2306211231_be.restservice.CustomerRestService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import apap.ti._5.accommodation_2306211231_be.restdto.BaseRequestDto;
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingUpdateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.response.accommodationbooking.AccommodationBookingDto;
import apap.ti._5.accommodation_2306211231_be.restservice.AccommodationBookingRestService;

class AccommodationBookingRestControllerMoreTests {
    private MockMvc mvc;
    private AccommodationBookingRestService service;
    private final ObjectMapper mapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @BeforeEach
    void setup() {
        service = Mockito.mock(AccommodationBookingRestService.class);
        var auth = Mockito.mock(AuthRestService.class);
        var review = Mockito.mock(AccommodationReviewRestService.class);
        var cust = Mockito.mock(CustomerRestService.class);
        var controller = new AccommodationBookingRestController(service, auth, review, cust);
        mvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    private AccommodationBookingDto sampleDto(String id) {
        return new AccommodationBookingDto(id, LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(4), 2, 100000, 0,
                UUID.randomUUID().toString(), "Alice", "a@a.com", "081234567890", true, 0, 0, 2, "ROOM-ID", "Prop", "Type", "Name", 100000, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void createBookingWithRoom_success() throws Exception {
    when(service.createBookingWithRoom(eq("RM-1"), any(apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingCreateRequest.class))).thenReturn(sampleDto("B1"));
        BaseRequestDto<AccommodationBookingCreateRequest> req = new BaseRequestDto<>();
        var data = new AccommodationBookingCreateRequest();
        data.setRoomId("RM-1");
        data.setCustomerId(UUID.randomUUID().toString());
        data.setCustomerName("Alice");
        data.setCustomerEmail("a@a.com");
        data.setCustomerPhone("081234567890");
        data.setCheckInDate(LocalDateTime.now().plusDays(2));
        data.setCheckOutDate(LocalDateTime.now().plusDays(4));
        req.setData(data);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/bookings/create/RM-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value(201))
            .andExpect(jsonPath("$.data.bookingId").value("B1"));
    }

    @Test
    void createBookingWithRoom_errorReturns500() throws Exception {
    when(service.createBookingWithRoom(eq("RM-ERR"), any(apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingCreateRequest.class))).thenThrow(new RuntimeException("fail create"));
        BaseRequestDto<AccommodationBookingCreateRequest> req = new BaseRequestDto<>();
        var data = new AccommodationBookingCreateRequest();
        data.setRoomId("RM-ERR");
        data.setCustomerId(UUID.randomUUID().toString());
        data.setCustomerName("Bob");
        data.setCustomerEmail("b@b.com");
        data.setCustomerPhone("081234567891");
        data.setCheckInDate(LocalDateTime.now().plusDays(3));
        data.setCheckOutDate(LocalDateTime.now().plusDays(5));
        req.setData(data);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/bookings/create/RM-ERR")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void updateBooking_success() throws Exception {
    when(service.updateBooking(eq("B2"), any(apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingUpdateRequest.class))).thenReturn(sampleDto("B2"));
        BaseRequestDto<AccommodationBookingUpdateRequest> req = new BaseRequestDto<>();
        var upd = new AccommodationBookingUpdateRequest();
        upd.setBookingId("B2");
        upd.setRoomId("ROOM-ID");
        upd.setRoomName("Name");
        upd.setRoomTypeName("Type");
        upd.setPropertyName("Prop");
        upd.setCustomerId(UUID.randomUUID().toString());
        upd.setCustomerName("Alice");
        upd.setCustomerEmail("a@a.com");
        upd.setCustomerPhone("081234567890");
        upd.setCheckInDate(LocalDateTime.now().plusDays(2));
        upd.setCheckOutDate(LocalDateTime.now().plusDays(4));
        req.setData(upd);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/bookings/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.bookingId").value("B2"));
    }

    @Test
    void chartInvalidParams_returns500() throws Exception {
        when(service.getBookingChart(anyInt(), anyInt(), Mockito.isNull(java.util.UUID.class))).thenThrow(new IllegalArgumentException("bad month"));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/bookings/chart")
                .queryParam("month", "13")
                .queryParam("year", "2025"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    void customersSuccess_returnsList() throws Exception {
        when(service.getCustomers()).thenReturn(java.util.List.of());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/bookings/customers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data").isArray());
    }
}
