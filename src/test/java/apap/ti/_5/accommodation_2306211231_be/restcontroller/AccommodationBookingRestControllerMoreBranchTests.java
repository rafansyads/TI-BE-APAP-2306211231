package apap.ti._5.accommodation_2306211231_be.restcontroller;

import apap.ti._5.accommodation_2306211231_be.models.*;
import apap.ti._5.accommodation_2306211231_be.models.profile.*;
import apap.ti._5.accommodation_2306211231_be.restdto.BaseRequestDto;
import apap.ti._5.accommodation_2306211231_be.restdto.BaseResponseDto;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AccommodationBookingRestControllerMoreBranchTests {

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

    // ========== getReviewDetail tests ==========

    @Test
    void getReviewDetail_superadmin_success() {
        setAuthentication("admin", "SUPERADMIN");
        
        Customer cust = new Customer();
        cust.setId(UUID.randomUUID());
        cust.setUsername("cust");
        
        Property prop = new Property();
        prop.setPropertyId("P001");
        
        AccommodationReview review = new AccommodationReview();
        review.setReviewId("R001");
        review.setCustomer(cust);
        review.setProperty(prop);
        
        when(reviewService.findById("R001")).thenReturn(Optional.of(review));
        
        ResponseEntity<BaseResponseDto<AccommodationReviewDTO>> response = controller.getReviewDetail("R001");
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void getReviewDetail_notFound() {
        setAuthentication("admin", "SUPERADMIN");
        when(reviewService.findById("NOTEXIST")).thenReturn(Optional.empty());
        
        ResponseEntity<BaseResponseDto<AccommodationReviewDTO>> response = controller.getReviewDetail("NOTEXIST");
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void getReviewDetail_owner_notOwnProperty_forbidden() {
        setAuthentication("owner1", "ACCOMMODATION_OWNER");
        
        UUID ownerId = UUID.randomUUID();
        UUID differentOwnerId = UUID.randomUUID();
        
        AccommodationOwner owner = new AccommodationOwner();
        owner.setId(ownerId);
        when(authRestService.findAggregateByUsername("owner1")).thenReturn(owner);
        
        Property prop = new Property();
        prop.setOwnerId(differentOwnerId); // different owner
        
        Customer cust = new Customer();
        cust.setId(UUID.randomUUID());
        
        AccommodationReview review = new AccommodationReview();
        review.setReviewId("R001");
        review.setProperty(prop);
        review.setCustomer(cust);
        
        when(reviewService.findById("R001")).thenReturn(Optional.of(review));
        
        ResponseEntity<BaseResponseDto<AccommodationReviewDTO>> response = controller.getReviewDetail("R001");
        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void getReviewDetail_owner_ownsProperty_success() {
        setAuthentication("owner1", "ACCOMMODATION_OWNER");
        
        UUID ownerId = UUID.randomUUID();
        
        AccommodationOwner owner = new AccommodationOwner();
        owner.setId(ownerId);
        when(authRestService.findAggregateByUsername("owner1")).thenReturn(owner);
        
        Property prop = new Property();
        prop.setOwnerId(ownerId);
        
        Customer cust = new Customer();
        cust.setId(UUID.randomUUID());
        cust.setUsername("cust");
        
        AccommodationReview review = new AccommodationReview();
        review.setReviewId("R001");
        review.setProperty(prop);
        review.setCustomer(cust);
        
        when(reviewService.findById("R001")).thenReturn(Optional.of(review));
        
        ResponseEntity<BaseResponseDto<AccommodationReviewDTO>> response = controller.getReviewDetail("R001");
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void getReviewDetail_customer_notReviewer_forbidden() {
        setAuthentication("cust2", "CUSTOMER");
        
        UUID customerId = UUID.randomUUID();
        UUID differentCustId = UUID.randomUUID();
        
        Customer caller = new Customer();
        caller.setId(customerId);
        when(authRestService.findAggregateByUsername("cust2")).thenReturn(caller);
        
        Customer reviewer = new Customer();
        reviewer.setId(differentCustId);
        
        Property prop = new Property();
        
        AccommodationReview review = new AccommodationReview();
        review.setReviewId("R001");
        review.setCustomer(reviewer);
        review.setProperty(prop);
        
        when(reviewService.findById("R001")).thenReturn(Optional.of(review));
        
        ResponseEntity<BaseResponseDto<AccommodationReviewDTO>> response = controller.getReviewDetail("R001");
        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void getReviewDetail_customer_isReviewer_success() {
        setAuthentication("cust1", "CUSTOMER");
        
        UUID customerId = UUID.randomUUID();
        
        Customer caller = new Customer();
        caller.setId(customerId);
        caller.setUsername("cust1");
        when(authRestService.findAggregateByUsername("cust1")).thenReturn(caller);
        
        Property prop = new Property();
        prop.setPropertyId("P001");
        
        AccommodationReview review = new AccommodationReview();
        review.setReviewId("R001");
        review.setCustomer(caller);
        review.setProperty(prop);
        
        when(reviewService.findById("R001")).thenReturn(Optional.of(review));
        
        ResponseEntity<BaseResponseDto<AccommodationReviewDTO>> response = controller.getReviewDetail("R001");
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void getReviewDetail_exception_returns500() {
        setAuthentication("admin", "SUPERADMIN");
        when(reviewService.findById(any())).thenThrow(new RuntimeException("DB error"));
        
        ResponseEntity<BaseResponseDto<AccommodationReviewDTO>> response = controller.getReviewDetail("R001");
        assertEquals(500, response.getStatusCode().value());
    }

    // ========== processCheckInToday tests ==========

    @Test
    void processCheckInToday_noAuth_unauthorized() {
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(ctx);
        
        ResponseEntity<BaseResponseDto<Map<String, Object>>> response = controller.processCheckInToday();
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void processCheckInToday_regularCustomer_forbidden() {
        setAuthentication("cust1", "CUSTOMER");
        
        ResponseEntity<BaseResponseDto<Map<String, Object>>> response = controller.processCheckInToday();
        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void processCheckInToday_owner_success() {
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
    void processCheckInToday_owner_notFound() {
        setAuthentication("owner1", "ACCOMMODATION_OWNER");
        when(authRestService.findAggregateByUsername("owner1")).thenReturn(null);
        
        ResponseEntity<BaseResponseDto<Map<String, Object>>> response = controller.processCheckInToday();
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void processCheckInToday_superadmin_success() {
        setAuthentication("admin", "SUPERADMIN");
        when(bookingService.processCheckInToday()).thenReturn(10);
        
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

    // ========== processCheckOutToday tests ==========

    @Test
    void processCheckOutToday_noAuth_unauthorized() {
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(ctx);
        
        ResponseEntity<BaseResponseDto<Map<String, Object>>> response = controller.processCheckOutToday();
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void processCheckOutToday_regularCustomer_forbidden() {
        setAuthentication("cust1", "CUSTOMER");
        
        ResponseEntity<BaseResponseDto<Map<String, Object>>> response = controller.processCheckOutToday();
        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void processCheckOutToday_owner_success() {
        setAuthentication("owner1", "ACCOMMODATION_OWNER");
        
        AccommodationOwner owner = new AccommodationOwner();
        owner.setId(UUID.randomUUID());
        when(authRestService.findAggregateByUsername("owner1")).thenReturn(owner);
        when(bookingService.processCheckoutPastDue()).thenReturn(3);
        
        ResponseEntity<BaseResponseDto<Map<String, Object>>> response = controller.processCheckOutToday();
        assertEquals(200, response.getStatusCode().value());
        assertEquals(3, response.getBody().getData().get("changed"));
    }

    @Test
    void processCheckOutToday_owner_notFound() {
        setAuthentication("owner1", "ACCOMMODATION_OWNER");
        when(authRestService.findAggregateByUsername("owner1")).thenReturn(null);
        
        ResponseEntity<BaseResponseDto<Map<String, Object>>> response = controller.processCheckOutToday();
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void processCheckOutToday_superadmin_success() {
        setAuthentication("admin", "SUPERADMIN");
        when(bookingService.processCheckoutPastDue()).thenReturn(7);
        
        ResponseEntity<BaseResponseDto<Map<String, Object>>> response = controller.processCheckOutToday();
        assertEquals(200, response.getStatusCode().value());
    }

    // ========== listReviewsByCustomer additional tests ==========

    @Test
    void listReviewsByCustomer_superadmin_success() {
        UUID custId = UUID.randomUUID();
        setAuthentication("admin", "SUPERADMIN");
        
        Customer cust = new Customer();
        cust.setId(custId);
        cust.setUsername("cust");
        
        Property prop = new Property();
        prop.setPropertyId("P001");
        
        AccommodationReview review = new AccommodationReview();
        review.setReviewId("R001");
        review.setCustomer(cust);
        review.setProperty(prop);
        
        when(reviewService.findByCustomerId(custId)).thenReturn(List.of(review));
        
        ResponseEntity<BaseResponseDto<List<AccommodationReviewDTO>>> response = 
            controller.listReviewsByCustomer(custId.toString());
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void listReviewsByCustomer_callerNull_forbidden() {
        UUID custId = UUID.randomUUID();
        setAuthentication("cust1", "CUSTOMER");
        when(authRestService.findAggregateByUsername("cust1")).thenReturn(null);
        
        ResponseEntity<BaseResponseDto<List<AccommodationReviewDTO>>> response = 
            controller.listReviewsByCustomer(custId.toString());
        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void listReviewsByCustomer_callerIdNull_forbidden() {
        UUID custId = UUID.randomUUID();
        setAuthentication("cust1", "CUSTOMER");
        
        Customer caller = new Customer();
        caller.setId(null);
        when(authRestService.findAggregateByUsername("cust1")).thenReturn(caller);
        
        ResponseEntity<BaseResponseDto<List<AccommodationReviewDTO>>> response = 
            controller.listReviewsByCustomer(custId.toString());
        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void listReviewsByCustomer_invalidUUID_badRequest() {
        setAuthentication("admin", "SUPERADMIN");
        
        ResponseEntity<BaseResponseDto<List<AccommodationReviewDTO>>> response = 
            controller.listReviewsByCustomer("invalid-uuid");
        assertEquals(400, response.getStatusCode().value());
    }

    // ========== createReview tests ==========

    @Test
    void createReview_noAuth_unauthorized() {
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(ctx);
        
        BaseRequestDto<AccommodationReviewDTO> request = new BaseRequestDto<>();
        ResponseEntity<BaseResponseDto<AccommodationReviewDTO>> response = controller.createReview(request);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void createReview_notCustomer_forbidden() {
        setAuthentication("owner", "ACCOMMODATION_OWNER");
        
        BaseRequestDto<AccommodationReviewDTO> request = new BaseRequestDto<>();
        ResponseEntity<BaseResponseDto<AccommodationReviewDTO>> response = controller.createReview(request);
        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void createReview_nullPayload_badRequest() {
        setAuthentication("cust1", "CUSTOMER");
        
        BaseRequestDto<AccommodationReviewDTO> request = new BaseRequestDto<>();
        request.setData(null);
        
        ResponseEntity<BaseResponseDto<AccommodationReviewDTO>> response = controller.createReview(request);
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void createReview_nullBookingId_badRequest() {
        setAuthentication("cust1", "CUSTOMER");
        
        AccommodationReviewDTO dto = AccommodationReviewDTO.builder().bookingId(null).build();
        BaseRequestDto<AccommodationReviewDTO> request = new BaseRequestDto<>();
        request.setData(dto);
        
        ResponseEntity<BaseResponseDto<AccommodationReviewDTO>> response = controller.createReview(request);
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void createReview_blankBookingId_badRequest() {
        setAuthentication("cust1", "CUSTOMER");
        
        AccommodationReviewDTO dto = AccommodationReviewDTO.builder().bookingId("   ").build();
        BaseRequestDto<AccommodationReviewDTO> request = new BaseRequestDto<>();
        request.setData(dto);
        
        ResponseEntity<BaseResponseDto<AccommodationReviewDTO>> response = controller.createReview(request);
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void createReview_bookingNotFound_notFound() {
        setAuthentication("cust1", "CUSTOMER");
        
        AccommodationReviewDTO dto = AccommodationReviewDTO.builder().bookingId("B001").build();
        BaseRequestDto<AccommodationReviewDTO> request = new BaseRequestDto<>();
        request.setData(dto);
        
        when(bookingService.getBookingById("B001")).thenReturn(Optional.empty());
        
        ResponseEntity<BaseResponseDto<AccommodationReviewDTO>> response = controller.createReview(request);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void createReview_bookingNotBelongsToCaller_forbidden() {
        setAuthentication("cust1", "CUSTOMER");
        
        UUID custId = UUID.randomUUID();
        UUID differentCustId = UUID.randomUUID();
        
        Customer caller = new Customer();
        caller.setId(custId);
        when(authRestService.findAggregateByUsername("cust1")).thenReturn(caller);
        
        AccommodationBooking booking = new AccommodationBooking();
        booking.setBookingId("B001");
        booking.setCustomerId(differentCustId);
        when(bookingService.getBookingById("B001")).thenReturn(Optional.of(booking));
        
        AccommodationReviewDTO dto = AccommodationReviewDTO.builder().bookingId("B001").build();
        BaseRequestDto<AccommodationReviewDTO> request = new BaseRequestDto<>();
        request.setData(dto);
        
        ResponseEntity<BaseResponseDto<AccommodationReviewDTO>> response = controller.createReview(request);
        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void createReview_bookingNotEligible_status0_badRequest() {
        setAuthentication("cust1", "CUSTOMER");
        
        UUID custId = UUID.randomUUID();
        Customer caller = new Customer();
        caller.setId(custId);
        when(authRestService.findAggregateByUsername("cust1")).thenReturn(caller);
        
        AccommodationBooking booking = new AccommodationBooking();
        booking.setBookingId("B001");
        booking.setCustomerId(custId);
        booking.setStatus(0); // Pending - not eligible
        when(bookingService.getBookingById("B001")).thenReturn(Optional.of(booking));
        
        AccommodationReviewDTO dto = AccommodationReviewDTO.builder().bookingId("B001").build();
        BaseRequestDto<AccommodationReviewDTO> request = new BaseRequestDto<>();
        request.setData(dto);
        
        ResponseEntity<BaseResponseDto<AccommodationReviewDTO>> response = controller.createReview(request);
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void createReview_duplicateReview_conflict() {
        setAuthentication("cust1", "CUSTOMER");
        
        UUID custId = UUID.randomUUID();
        Customer caller = new Customer();
        caller.setId(custId);
        when(authRestService.findAggregateByUsername("cust1")).thenReturn(caller);
        
        AccommodationBooking booking = new AccommodationBooking();
        booking.setBookingId("B001");
        booking.setCustomerId(custId);
        booking.setStatus(4); // Done - eligible
        when(bookingService.getBookingById("B001")).thenReturn(Optional.of(booking));
        
        when(reviewService.existsForBooking("B001")).thenReturn(true); // Already reviewed
        
        AccommodationReviewDTO dto = AccommodationReviewDTO.builder().bookingId("B001").build();
        BaseRequestDto<AccommodationReviewDTO> request = new BaseRequestDto<>();
        request.setData(dto);
        
        ResponseEntity<BaseResponseDto<AccommodationReviewDTO>> response = controller.createReview(request);
        assertEquals(409, response.getStatusCode().value());
    }

    @Test
    void createReview_success_status4() {
        setAuthentication("cust1", "CUSTOMER");
        
        UUID custId = UUID.randomUUID();
        Customer cust = new Customer();
        cust.setId(custId);
        cust.setUsername("cust1");
        when(authRestService.findAggregateByUsername("cust1")).thenReturn(cust);
        when(customerService.findById(custId)).thenReturn(Optional.of(cust));
        
        Property prop = new Property();
        prop.setPropertyId("P001");
        
        RoomType rt = new RoomType();
        rt.setProperty(prop);
        
        Room room = new Room();
        room.setRoomType(rt);
        
        AccommodationBooking booking = new AccommodationBooking();
        booking.setBookingId("B001");
        booking.setCustomerId(custId);
        booking.setStatus(4); // Done
        booking.setRoom(room);
        when(bookingService.getBookingById("B001")).thenReturn(Optional.of(booking));
        
        when(reviewService.existsForBooking("B001")).thenReturn(false);
        
        AccommodationReview savedReview = new AccommodationReview();
        savedReview.setReviewId("R001");
        savedReview.setCustomer(cust);
        savedReview.setProperty(prop);
        when(reviewService.create(any())).thenReturn(savedReview);
        
        AccommodationReviewDTO dto = AccommodationReviewDTO.builder()
            .bookingId("B001")
            .overallRating(5)
            .cleanlinessRating(5)
            .facilityRating(5)
            .serviceRating(5)
            .valueRating(5)
            .comment("Great!")
            .build();
        BaseRequestDto<AccommodationReviewDTO> request = new BaseRequestDto<>();
        request.setData(dto);
        
        ResponseEntity<BaseResponseDto<AccommodationReviewDTO>> response = controller.createReview(request);
        assertEquals(201, response.getStatusCode().value());
    }
}
