package apap.ti._5.accommodation_2306211231_be.restcontroller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import jakarta.persistence.EntityManagerFactory;

import apap.ti._5.accommodation_2306211231_be.restdto.BaseRequestDto;
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingRefundRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingCancelRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingPayRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingUpdateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.response.accommodationbooking.AccommodationBookingDto;
import apap.ti._5.accommodation_2306211231_be.restdto.response.profile.CustomerSummaryResponseDTO;
import apap.ti._5.accommodation_2306211231_be.restservice.AccommodationBookingRestService;
import apap.ti._5.accommodation_2306211231_be.restservice.AuthRestService;
import apap.ti._5.accommodation_2306211231_be.restservice.AccommodationReviewRestService;
import apap.ti._5.accommodation_2306211231_be.restservice.CustomerRestService;
import apap.ti._5.accommodation_2306211231_be.security.jwt.JwtUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = AccommodationBookingRestController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class, DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
class AccommodationBookingRestControllerIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean AccommodationBookingRestService service;
    @MockBean AuthRestService authRestService;
    @MockBean AccommodationReviewRestService reviewService;
    @MockBean CustomerRestService customerService;
    @MockBean JwtUtils jwtUtils;
    @MockBean UserDetailsService userDetailsService;
    @MockBean EntityManagerFactory entityManagerFactory;

    @BeforeEach
    void setupAuth() {
        // default to superadmin so controller will return global results in tests
        var auth = new TestingAuthenticationToken("super", "N/A", List.of(new SimpleGrantedAuthority("ROLE_SUPERADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clearAuth() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listBookings_returnsWrappedDto() throws Exception {
        var dto = new AccommodationBookingDto();
        dto.setBookingId("B-1");
        when(service.getAllBookingsDto()).thenReturn(List.of(dto));
        mockMvc.perform(get("/bookings").accept(MediaType.APPLICATION_JSON))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data[0].bookingId").value("B-1"));
    }

    @Test
    void getBooking_byId_success() throws Exception {
        var dto = new AccommodationBookingDto();
        dto.setBookingId("B-1");
        when(service.getBookingDtoById("B-1")).thenReturn(dto);
        mockMvc.perform(get("/bookings/B-1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.bookingId").value("B-1"));
    }

    @Test
    void createBooking_success() throws Exception {
        var req = new BaseRequestDto<AccommodationBookingCreateRequest>();
        var data = new AccommodationBookingCreateRequest();
        data.setRoomId("RM-1");
        req.setData(data);
        var dto = new AccommodationBookingDto();
        dto.setBookingId("B-NEW");
        when(service.createBooking(any(AccommodationBookingCreateRequest.class))).thenReturn(dto); // keys used by controller
        mockMvc.perform(post("/bookings/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.bookingId").value("B-NEW"));
    }

    @Test
    void statusEndpoints_ok() throws Exception {
        var dto = new AccommodationBookingDto();
        dto.setBookingId("B-1");
        when(service.markBookingAsPaid("B-1")).thenReturn(dto);
        when(service.cancelBooking("B-1")).thenReturn(dto);
        when(service.refundBooking("B-1")).thenReturn(dto);
        when(service.processCheckInToday()).thenReturn(3);

        var payReq = new BaseRequestDto<AccommodationBookingPayRequest>();
        var pay = new AccommodationBookingPayRequest();
        pay.setBookingId("B-1");
        payReq.setData(pay);
        mockMvc.perform(post("/bookings/status/pay").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(payReq)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.bookingId").value("B-1"));

        var cancelReq = new BaseRequestDto<AccommodationBookingCancelRequest>();
        var cancel = new AccommodationBookingCancelRequest();
        cancel.setBookingId("B-1");
        cancelReq.setData(cancel);
        mockMvc.perform(post("/bookings/status/cancel").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(cancelReq)))
            .andExpect(status().isOk());

        var refundReq = new BaseRequestDto<AccommodationBookingRefundRequest>();
        var refund = new AccommodationBookingRefundRequest();
        refund.setBookingId("B-1");
        refundReq.setData(refund);
        mockMvc.perform(post("/bookings/status/refund").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(refundReq)))
            .andExpect(status().isOk());

        mockMvc.perform(post("/bookings/status/process-checkin").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.changed").value(3));
    }

    @Test
    void chartAndCustomers_ok() throws Exception {
        when(service.getBookingChart(11, 2025, null)).thenReturn(Map.of("labels", List.of("Hotel A"), "values", List.of(100)));
        mockMvc.perform(get("/bookings/chart").param("month", "11").param("year", "2025"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.labels[0]").value("Hotel A"));

        when(service.getCustomers()).thenReturn(List.of(new CustomerSummaryResponseDTO("id","Alice","a@a.com","+62")));
        mockMvc.perform(get("/bookings/customers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].customerName").value("Alice"));
    }
}
