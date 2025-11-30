package apap.ti._5.accommodation_2306211231_be.restcontroller;

import apap.ti._5.accommodation_2306211231_be.models.AccommodationBooking;
import apap.ti._5.accommodation_2306211231_be.models.AccommodationReview;
import apap.ti._5.accommodation_2306211231_be.models.Property;
import apap.ti._5.accommodation_2306211231_be.models.Room;
import apap.ti._5.accommodation_2306211231_be.models.RoomType;
import apap.ti._5.accommodation_2306211231_be.models.profile.AccommodationOwner;
import apap.ti._5.accommodation_2306211231_be.models.profile.Customer;
import apap.ti._5.accommodation_2306211231_be.restdto.BaseRequestDto;
import apap.ti._5.accommodation_2306211231_be.restdto.BaseResponseDto;
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.*;
import apap.ti._5.accommodation_2306211231_be.restdto.response.accommodationbooking.*;
import apap.ti._5.accommodation_2306211231_be.restservice.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccommodationBookingRestControllerBranchTests {

    @Mock private AccommodationBookingRestService bookingService;
    @Mock private AuthRestService authRestService;
    @Mock private AccommodationReviewRestService reviewService;
    @Mock private CustomerRestService customerService;

    private AccommodationBookingRestController controller;
    private SecurityContext originalContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new AccommodationBookingRestController(bookingService, authRestService, reviewService, customerService);
        originalContext = SecurityContextHolder.getContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.setContext(originalContext);
    }

    private void setAuthentication(String name, String... roles) {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(name);
        List<SimpleGrantedAuthority> authorities = Arrays.stream(roles)
                .map(SimpleGrantedAuthority::new)
                .toList();
        doReturn(authorities).when(auth).getAuthorities();
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }

    @Test
    void listBookings_superadmin_returnsAllBookings() {
        setAuthentication("admin", "SUPERADMIN");
        when(bookingService.getAllBookingsDto()).thenReturn(List.of(new AccommodationBookingDto()));

        ResponseEntity<BaseResponseDto<ArrayList<AccommodationBookingDto>>> response = controller.listBookings();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().size());
    }

    @Test
    void listBookings_superadminWithRole_returnsAllBookings() {
        setAuthentication("admin", "ROLE_SUPERADMIN");
        when(bookingService.getAllBookingsDto()).thenReturn(List.of(new AccommodationBookingDto()));

        ResponseEntity<BaseResponseDto<ArrayList<AccommodationBookingDto>>> response = controller.listBookings();

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void listBookings_superadminNullList_returnsEmptyList() {
        setAuthentication("admin", "SUPERADMIN");
        when(bookingService.getAllBookingsDto()).thenReturn(null);

        ResponseEntity<BaseResponseDto<ArrayList<AccommodationBookingDto>>> response = controller.listBookings();

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().getData().isEmpty());
    }

    @Test
    void listBookings_owner_returnsOwnedBookings() {
        setAuthentication("owner1", "ACCOMMODATION_OWNER");
        
        AccommodationOwner owner = new AccommodationOwner();
        owner.setId(UUID.randomUUID());
        when(authRestService.findAggregateByUsername("owner1")).thenReturn(owner);
        
        Property prop = new Property();
        prop.setOwnerId(owner.getId());
        RoomType rt = new RoomType();
        rt.setProperty(prop);
        Room room = new Room();
        room.setRoomType(rt);
        AccommodationBooking booking = new AccommodationBooking();
        booking.setRoom(room);
        booking.setBookingId("B001");
        
        when(bookingService.getAllBookings()).thenReturn(List.of(booking));

        ResponseEntity<BaseResponseDto<ArrayList<AccommodationBookingDto>>> response = controller.listBookings();

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void listBookings_ownerNotFound_returns404() {
        setAuthentication("owner1", "ACCOMMODATION_OWNER");
        when(authRestService.findAggregateByUsername("owner1")).thenReturn(null);

        ResponseEntity<BaseResponseDto<ArrayList<AccommodationBookingDto>>> response = controller.listBookings();

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void listBookings_customer_returnsOwnBookings() {
        setAuthentication("cust1", "CUSTOMER");
        
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        when(authRestService.findAggregateByUsername("cust1")).thenReturn(customer);
        
        AccommodationBooking booking = new AccommodationBooking();
        booking.setCustomerId(customer.getId());
        booking.setBookingId("B001");
        
        when(bookingService.getAllBookings()).thenReturn(List.of(booking));

        ResponseEntity<BaseResponseDto<ArrayList<AccommodationBookingDto>>> response = controller.listBookings();

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void listBookings_customerNotFound_returns404() {
        setAuthentication("cust1", "CUSTOMER");
        when(authRestService.findAggregateByUsername("cust1")).thenReturn(null);

        ResponseEntity<BaseResponseDto<ArrayList<AccommodationBookingDto>>> response = controller.listBookings();

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void listBookings_noAuth_returnsForbidden() {
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(ctx);

        ResponseEntity<BaseResponseDto<ArrayList<AccommodationBookingDto>>> response = controller.listBookings();

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void listBookings_exception_returns500() {
        setAuthentication("admin", "SUPERADMIN");
        when(bookingService.getAllBookingsDto()).thenThrow(new RuntimeException("DB error"));

        ResponseEntity<BaseResponseDto<ArrayList<AccommodationBookingDto>>> response = controller.listBookings();

        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    void listReviewsByCustomer_blankCustomerId_returns400() {
        setAuthentication("cust1", "CUSTOMER");

        ResponseEntity<BaseResponseDto<List<AccommodationReviewDTO>>> response = 
            controller.listReviewsByCustomer("   ");

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void listReviewsByCustomer_nullCustomerId_returns400() {
        setAuthentication("cust1", "CUSTOMER");

        ResponseEntity<BaseResponseDto<List<AccommodationReviewDTO>>> response = 
            controller.listReviewsByCustomer(null);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void listReviewsByCustomer_customerAccessingOtherCustomer_returnsForbidden() {
        UUID callerId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        
        setAuthentication("cust1", "CUSTOMER");
        
        Customer caller = new Customer();
        caller.setId(callerId);
        when(authRestService.findAggregateByUsername("cust1")).thenReturn(caller);

        ResponseEntity<BaseResponseDto<List<AccommodationReviewDTO>>> response = 
            controller.listReviewsByCustomer(otherId.toString());

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void listReviewsByCustomer_customerCallerNull_returnsForbidden() {
        UUID custId = UUID.randomUUID();
        setAuthentication("cust1", "CUSTOMER");
        when(authRestService.findAggregateByUsername("cust1")).thenReturn(null);

        ResponseEntity<BaseResponseDto<List<AccommodationReviewDTO>>> response = 
            controller.listReviewsByCustomer(custId.toString());

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void listReviewsByCustomer_invalidUUID_returns400() {
        setAuthentication("cust1", "SUPERADMIN");

        ResponseEntity<BaseResponseDto<List<AccommodationReviewDTO>>> response = 
            controller.listReviewsByCustomer("invalid-uuid");

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void listReviewsByCustomer_success_returnsReviews() {
        UUID custId = UUID.randomUUID();
        setAuthentication("cust1", "CUSTOMER");
        
        Customer caller = new Customer();
        caller.setId(custId);
        caller.setUsername("cust1");
        when(authRestService.findAggregateByUsername("cust1")).thenReturn(caller);
        
        // Set up property for the review
        Property prop = new Property();
        prop.setPropertyId("PROP001");
        
        AccommodationReview review = new AccommodationReview();
        review.setReviewId("R001");
        review.setProperty(prop);
        review.setCustomer(caller);
        when(reviewService.findByCustomerId(custId)).thenReturn(List.of(review));

        ResponseEntity<BaseResponseDto<List<AccommodationReviewDTO>>> response = 
            controller.listReviewsByCustomer(custId.toString());

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void listReviewsByCustomer_exception_returns500() {
        UUID custId = UUID.randomUUID();
        setAuthentication("admin", "SUPERADMIN");
        when(reviewService.findByCustomerId(custId)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<BaseResponseDto<List<AccommodationReviewDTO>>> response = 
            controller.listReviewsByCustomer(custId.toString());

        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    void processCheckInToday_nullAuth_returnsUnauthorized() {
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(ctx);

        ResponseEntity<BaseResponseDto<Map<String, Object>>> response = controller.processCheckInToday();

        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void processCheckInToday_customer_returnsForbidden() {
        setAuthentication("cust1", "CUSTOMER");

        ResponseEntity<BaseResponseDto<Map<String, Object>>> response = controller.processCheckInToday();

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void processCheckInToday_ownerNotFound_returns404() {
        setAuthentication("owner1", "ACCOMMODATION_OWNER");
        when(authRestService.findAggregateByUsername("owner1")).thenReturn(null);

        ResponseEntity<BaseResponseDto<Map<String, Object>>> response = controller.processCheckInToday();

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void processCheckInToday_ownerIdNull_returns404() {
        setAuthentication("owner1", "ACCOMMODATION_OWNER");
        AccommodationOwner owner = new AccommodationOwner();
        owner.setId(null);
        when(authRestService.findAggregateByUsername("owner1")).thenReturn(owner);

        ResponseEntity<BaseResponseDto<Map<String, Object>>> response = controller.processCheckInToday();

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void processCheckInToday_ownerSuccess_returnsOk() {
        setAuthentication("owner1", "ACCOMMODATION_OWNER");
        AccommodationOwner owner = new AccommodationOwner();
        owner.setId(UUID.randomUUID());
        when(authRestService.findAggregateByUsername("owner1")).thenReturn(owner);
        when(bookingService.processCheckInToday()).thenReturn(5);

        ResponseEntity<BaseResponseDto<Map<String, Object>>> response = controller.processCheckInToday();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(5, response.getBody().getData().get("changed"));
    }

    @Test
    void processCheckInToday_superadmin_returnsOk() {
        setAuthentication("admin", "SUPERADMIN");
        when(bookingService.processCheckInToday()).thenReturn(3);

        ResponseEntity<BaseResponseDto<Map<String, Object>>> response = controller.processCheckInToday();

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void processCheckInToday_exception_returns500() {
        setAuthentication("admin", "SUPERADMIN");
        when(bookingService.processCheckInToday()).thenThrow(new RuntimeException("Error"));

        ResponseEntity<BaseResponseDto<Map<String, Object>>> response = controller.processCheckInToday();

        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    void processCheckOutToday_nullAuth_returnsUnauthorized() {
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(ctx);

        ResponseEntity<BaseResponseDto<Map<String, Object>>> response = controller.processCheckOutToday();

        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void processCheckOutToday_ownerNotFound_returns404() {
        setAuthentication("owner1", "ACCOMMODATION_OWNER");
        when(authRestService.findAggregateByUsername("owner1")).thenReturn(null);

        ResponseEntity<BaseResponseDto<Map<String, Object>>> response = controller.processCheckOutToday();

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void processCheckOutToday_success_returnsOk() {
        setAuthentication("admin", "SUPERADMIN");
        when(bookingService.processCheckoutPastDue()).thenReturn(2);

        ResponseEntity<BaseResponseDto<Map<String, Object>>> response = controller.processCheckOutToday();

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void getBooking_superadmin_returnsBooking() {
        setAuthentication("admin", "SUPERADMIN");
        AccommodationBookingDto dto = new AccommodationBookingDto();
        dto.setBookingId("B001");
        when(bookingService.getBookingDtoById("B001")).thenReturn(dto);

        ResponseEntity<BaseResponseDto<AccommodationBookingDto>> response = controller.getBooking("B001");

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void getBooking_bookingNotFound_returns404() {
        setAuthentication("owner1", "ACCOMMODATION_OWNER");
        when(bookingService.getBookingById("B001")).thenReturn(Optional.empty());

        ResponseEntity<BaseResponseDto<AccommodationBookingDto>> response = controller.getBooking("B001");

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void getBooking_ownerNotOwningBooking_returnsForbidden() {
        setAuthentication("owner1", "ACCOMMODATION_OWNER");
        
        UUID ownerId = UUID.randomUUID();
        AccommodationOwner owner = new AccommodationOwner();
        owner.setId(ownerId);
        when(authRestService.findAggregateByUsername("owner1")).thenReturn(owner);
        
        Property prop = new Property();
        prop.setOwnerId(UUID.randomUUID()); // Different owner
        RoomType rt = new RoomType();
        rt.setProperty(prop);
        Room room = new Room();
        room.setRoomType(rt);
        AccommodationBooking booking = new AccommodationBooking();
        booking.setRoom(room);
        
        when(bookingService.getBookingById("B001")).thenReturn(Optional.of(booking));

        ResponseEntity<BaseResponseDto<AccommodationBookingDto>> response = controller.getBooking("B001");

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void getBooking_customerNotOwningBooking_returnsForbidden() {
        setAuthentication("cust1", "CUSTOMER");
        
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        when(authRestService.findAggregateByUsername("cust1")).thenReturn(customer);
        
        AccommodationBooking booking = new AccommodationBooking();
        booking.setCustomerId(UUID.randomUUID()); // Different customer
        
        when(bookingService.getBookingById("B001")).thenReturn(Optional.of(booking));

        ResponseEntity<BaseResponseDto<AccommodationBookingDto>> response = controller.getBooking("B001");

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void getBookingChart_ownerCallerNull_returnsUnauthorized() {
        setAuthentication(null, "ACCOMMODATION_OWNER");

        ResponseEntity<BaseResponseDto<Map<String, Object>>> response = controller.getBookingChart(1, 2024);

        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void getBookingChart_ownerNotFound_returns404() {
        setAuthentication("owner1", "ACCOMMODATION_OWNER");
        when(authRestService.findAggregateByUsername("owner1")).thenReturn(null);

        ResponseEntity<BaseResponseDto<Map<String, Object>>> response = controller.getBookingChart(1, 2024);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void getBookingChart_ownerIdNull_returns404() {
        setAuthentication("owner1", "ACCOMMODATION_OWNER");
        AccommodationOwner owner = new AccommodationOwner();
        owner.setId(null);
        when(authRestService.findAggregateByUsername("owner1")).thenReturn(owner);

        ResponseEntity<BaseResponseDto<Map<String, Object>>> response = controller.getBookingChart(1, 2024);

        assertEquals(404, response.getStatusCode().value());
    }
}
