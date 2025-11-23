package apap.ti._5.accommodation_2306211231_be.restcontroller;

import apap.ti._5.accommodation_2306211231_be.restdto.BaseRequestDto;
import apap.ti._5.accommodation_2306211231_be.restdto.BaseResponseDto;
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingCancelRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingPayRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingRefundRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingUpdateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.response.accommodationbooking.AccommodationBookingDto;
import apap.ti._5.accommodation_2306211231_be.restdto.response.profile.CustomerSummaryResponseDTO;
import apap.ti._5.accommodation_2306211231_be.restservice.AccommodationBookingRestService;
import apap.ti._5.accommodation_2306211231_be.restservice.AccommodationReviewRestService;
import apap.ti._5.accommodation_2306211231_be.restmapper.AccommodationReviewMapper;
import apap.ti._5.accommodation_2306211231_be.restdto.response.accommodationbooking.AccommodationReviewDTO;
import apap.ti._5.accommodation_2306211231_be.restservice.CustomerRestService;
import apap.ti._5.accommodation_2306211231_be.util.ResponseUtil;
import apap.ti._5.accommodation_2306211231_be.restmapper.AccommodationBookingMapper;
import apap.ti._5.accommodation_2306211231_be.restservice.AuthRestService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class AccommodationBookingRestController {

    private final AccommodationBookingRestService bookingService;
    private final AuthRestService authRestService;
    private final AccommodationReviewRestService reviewService;
    private final CustomerRestService customerService;

    @GetMapping
    public ResponseEntity<BaseResponseDto<ArrayList<AccommodationBookingDto>>> listBookings() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            String caller = (auth != null) ? auth.getName() : null;
            boolean isOwner = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ACCOMMODATION_OWNER")
                            || a.getAuthority().equals("ROLE_ACCOMMODATION_OWNER"));
            boolean isSuper = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("SUPERADMIN") || a.getAuthority().equals("ROLE_SUPERADMIN"));
            boolean isCustomer = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("CUSTOMER") || a.getAuthority().equals("ROLE_CUSTOMER"));

            ArrayList<AccommodationBookingDto> bookings = new ArrayList<>();
            if (isSuper) {
                var bookingsList = bookingService.getAllBookingsDto();
                bookings = bookingsList == null ? new ArrayList<>() : new ArrayList<>(bookingsList);
            } else if (isOwner && caller != null) {
                var user = authRestService.findAggregateByUsername(caller);
                if (user == null)
                    return ResponseUtil.error("Owner not found", HttpStatus.NOT_FOUND);
                var ownerId = user.getId();
                var entities = bookingService.getAllBookings();
                for (var b : entities) {
                    if (b.getRoom() == null || b.getRoom().getRoomType() == null || b.getRoom().getRoomType().getProperty() == null)
                        continue;
                    var prop = b.getRoom().getRoomType().getProperty();
                    if (prop.getOwnerId() != null && prop.getOwnerId().equals(ownerId)) {
                        bookings.add(AccommodationBookingMapper.toDto(b));
                    }
                }
            } else if (isCustomer && caller != null) {
                var user = authRestService.findAggregateByUsername(caller);
                if (user == null)
                    return ResponseUtil.error("Customer not found", HttpStatus.NOT_FOUND);
                var custId = user.getId();
                var entities = bookingService.getAllBookings();
                for (var b : entities) {
                    if (b.getCustomerId() != null && b.getCustomerId().equals(custId)) {
                        bookings.add(AccommodationBookingMapper.toDto(b));
                    }
                }
            } else {
                // default: deny or empty
                return ResponseUtil.error("Forbidden", HttpStatus.FORBIDDEN);
            }

            if (bookings.isEmpty()) {
                return ResponseUtil.success(
                        new ArrayList<AccommodationBookingDto>(),
                        "[GET] No bookings found",
                        HttpStatus.OK).toBuilder().build();
            }
            return ResponseUtil.success(
                    bookings,
                    "[GET] All bookings retrieved successfully with total count: " + bookings.size(),
                    HttpStatus.OK).toBuilder().build();
        } catch (Exception ex) {
            return ResponseUtil.error(
                    "An error occurred while fetching bookings: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/reviews")
    public ResponseEntity<BaseResponseDto<java.util.List<AccommodationReviewDTO>>> listReviewsByCustomer(
            @RequestParam(value = "customerID", required = true) String customerIdStr) {
        try {
            if (customerIdStr == null || customerIdStr.isBlank()) {
                return ResponseUtil.error("customerID is required", HttpStatus.BAD_REQUEST);
            }
            java.util.UUID custId = java.util.UUID.fromString(customerIdStr.trim());

            var auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isCustomer = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("CUSTOMER") || a.getAuthority().equals("ROLE_CUSTOMER"));
            boolean isSuper = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("SUPERADMIN") || a.getAuthority().equals("ROLE_SUPERADMIN"));

            if (isCustomer) {
                String caller = auth.getName();
                var callerAgg = authRestService.findAggregateByUsername(caller);
                if (callerAgg == null || callerAgg.getId() == null || !callerAgg.getId().equals(custId)) {
                    return ResponseUtil.error("Forbidden", HttpStatus.FORBIDDEN);
                }
            }

            var reviews = reviewService.findByCustomerId(custId);
            var dtos = reviews.stream().map(AccommodationReviewMapper::toDTO).toList();
            return ResponseUtil.success(dtos, "[GET] Reviews for customer retrieved", HttpStatus.OK).toBuilder().build();
        } catch (IllegalArgumentException ex) {
            return ResponseUtil.error(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            return ResponseUtil.error("An error occurred while fetching reviews: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/reviews/{id}")
    public ResponseEntity<BaseResponseDto<AccommodationReviewDTO>> getReviewDetail(@PathVariable("id") String id) {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            String caller = (auth != null) ? auth.getName() : null;
            boolean isOwner = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ACCOMMODATION_OWNER") || a.getAuthority().equals("ROLE_ACCOMMODATION_OWNER"));
            boolean isSuper = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("SUPERADMIN") || a.getAuthority().equals("ROLE_SUPERADMIN"));
            boolean isCustomer = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("CUSTOMER") || a.getAuthority().equals("ROLE_CUSTOMER"));

            var revOpt = reviewService.findById(id);
            if (revOpt.isEmpty()) return ResponseUtil.error("Review not found", HttpStatus.NOT_FOUND);
            var rev = revOpt.get();

            if (isOwner && caller != null && !isSuper) {
                // verify owner owns the reviewed property, superadmin can bypass
                var callerAgg = authRestService.findAggregateByUsername(caller);
                String ownerId = rev.getProperty() == null ? null : rev.getProperty().getOwnerId() == null ? null
                        : rev.getProperty().getOwnerId().toString();
                if (callerAgg == null || ownerId == null || !ownerId.equals(callerAgg.getId() == null ? null : callerAgg.getId().toString())) {
                    return ResponseUtil.error("Forbidden", HttpStatus.FORBIDDEN);
                }
            }
            if (isCustomer && caller != null && !isSuper) {
                // verify customer is the reviewer, superadmin can bypass
                var callerAgg = authRestService.findAggregateByUsername(caller);
                if (callerAgg == null || callerAgg.getId() == null || !callerAgg.getId().equals(rev.getCustomer().getId())) {
                    return ResponseUtil.error("Forbidden", HttpStatus.FORBIDDEN);
                }
            }

            var dto = AccommodationReviewMapper.toDTO(rev);
            return ResponseUtil.success(dto, "[GET] Review detail retrieved", HttpStatus.OK).toBuilder().build();
        } catch (Exception ex) {
            return ResponseUtil.error("An error occurred while fetching review: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/reviews/create")
    public ResponseEntity<BaseResponseDto<AccommodationReviewDTO>> createReview(
            @Validated @RequestBody BaseRequestDto<AccommodationReviewDTO> request) {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) return ResponseUtil.error("Unauthorized", HttpStatus.UNAUTHORIZED);
            boolean isCustomer = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("CUSTOMER") || a.getAuthority().equals("ROLE_CUSTOMER"));
            if (!isCustomer) return ResponseUtil.error("Forbidden", HttpStatus.FORBIDDEN);

            var payload = request.getData();
            if (payload == null || payload.getBookingId() == null || payload.getBookingId().isBlank()) {
                return ResponseUtil.error("bookingId is required", HttpStatus.BAD_REQUEST);
            }

            // Validate booking belongs to caller and is completed (or payment confirmed and checkout passed)
            var maybeBooking = bookingService.getBookingById(payload.getBookingId());
            if (maybeBooking.isEmpty()) return ResponseUtil.error("Booking not found", HttpStatus.NOT_FOUND);
            var booking = maybeBooking.get();

            var callerAgg = authRestService.findAggregateByUsername(auth.getName());
            if (callerAgg == null || callerAgg.getId() == null || !callerAgg.getId().equals(booking.getCustomerId())) {
                return ResponseUtil.error("Forbidden: booking does not belong to caller", HttpStatus.FORBIDDEN);
            }

            // Booking must be payment confirmed and checkout passed OR status done
            java.time.LocalDateTime checkout = booking.getCheckOutDate();
            int st = booking.getStatus() == null ? 0 : booking.getStatus();
            java.time.LocalDateTime now = java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Jakarta")).toLocalDateTime();
            boolean allowed = (st == 4) || (st == 1 && checkout != null && !now.isBefore(checkout));
            if (!allowed) return ResponseUtil.error("Booking is not eligible for review yet", HttpStatus.BAD_REQUEST);

            // Prevent duplicate review for same booking
            if (reviewService.existsForBooking(booking.getBookingId())) {
                return ResponseUtil.error("Review already exists for this booking", HttpStatus.CONFLICT);
            }

            // Build review entity
            apap.ti._5.accommodation_2306211231_be.models.AccommodationReview review =
                    apap.ti._5.accommodation_2306211231_be.models.AccommodationReview.builder()
                    .reviewId(java.util.UUID.randomUUID().toString())
                    .booking(booking)
                    .property(booking.getRoom().getRoomType().getProperty())
                    .customer(customerService.findById(booking.getCustomerId()).orElse(null))
                    .overallRating(payload.getOverallRating())
                    .cleanlinessRating(payload.getCleanlinessRating())
                    .facilityRating(payload.getFacilityRating())
                    .serviceRating(payload.getServiceRating())
                    .valueRating(payload.getValueRating())
                    .comment(payload.getComment())
                    .build();

            var saved = reviewService.create(review);
            var dto = AccommodationReviewMapper.toDTO(saved);
            return ResponseUtil.success(dto, "[POST] Review created", HttpStatus.CREATED).toBuilder().build();
        } catch (IllegalArgumentException ex) {
            return ResponseUtil.error(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            return ResponseUtil.error("An error occurred while creating review: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/status/process-checkin")
    public ResponseEntity<BaseResponseDto<Map<String, Object>>> processCheckInToday() {
        int changed = bookingService.processCheckInToday();
        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("changed", changed);
        return ResponseUtil.success(
                payload,
                "[POST] Processed check-in for today, total changed: " + changed,
                HttpStatus.OK).toBuilder().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponseDto<AccommodationBookingDto>> getBooking(@PathVariable("id") String id) {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            String caller = (auth != null) ? auth.getName() : null;
            boolean isOwner = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ACCOMMODATION_OWNER")
                            || a.getAuthority().equals("ROLE_ACCOMMODATION_OWNER"));
            boolean isSuper = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("SUPERADMIN") || a.getAuthority().equals("ROLE_SUPERADMIN"));
            boolean isCustomer = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("CUSTOMER") || a.getAuthority().equals("ROLE_CUSTOMER"));

            if (isSuper) {
                AccommodationBookingDto dto = bookingService.getBookingDtoById(id);
                return ResponseUtil.success(dto, "[GET] Booking retrieved successfully for ID: " + id, HttpStatus.OK).toBuilder().build();
            }

            // Need to check ownership / customer identity
            var entityOpt = bookingService.getBookingById(id);
            if (entityOpt.isEmpty()) throw new NoSuchElementException("Booking not found with ID: " + id);
            var entity = entityOpt.get();

            if (isOwner && caller != null) {
                var user = authRestService.findAggregateByUsername(caller);
                if (user == null) return ResponseUtil.error("Owner not found", HttpStatus.NOT_FOUND);
                var prop = entity.getRoom() == null || entity.getRoom().getRoomType() == null ? null : entity.getRoom().getRoomType().getProperty();
                if (prop == null || prop.getOwnerId() == null || !prop.getOwnerId().equals(user.getId())) {
                    return ResponseUtil.error("Forbidden", HttpStatus.FORBIDDEN);
                }
                AccommodationBookingDto dto = AccommodationBookingMapper.toDto(entity);
                return ResponseUtil.success(dto, "[GET] Booking retrieved successfully for ID: " + id, HttpStatus.OK).toBuilder().build();
            }

            if (isCustomer && caller != null) {
                var user = authRestService.findAggregateByUsername(caller);
                if (user == null) return ResponseUtil.error("Customer not found", HttpStatus.NOT_FOUND);
                if (entity.getCustomerId() == null || !entity.getCustomerId().equals(user.getId())) {
                    return ResponseUtil.error("Forbidden", HttpStatus.FORBIDDEN);
                }
                AccommodationBookingDto dto = AccommodationBookingMapper.toDto(entity);
                return ResponseUtil.success(dto, "[GET] Booking retrieved successfully for ID: " + id, HttpStatus.OK).toBuilder().build();
            }

            return ResponseUtil.error("Forbidden", HttpStatus.FORBIDDEN);
        } catch (NoSuchElementException ex) {
            return ResponseUtil.error(
                    ex.getMessage(),
                    HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<BaseResponseDto<AccommodationBookingDto>> createBooking(
            @Validated @RequestBody BaseRequestDto<AccommodationBookingCreateRequest> request) {
        try {
            AccommodationBookingDto dto = bookingService.createBooking(request.getData());
            return ResponseUtil.success(
                    dto,
                    "[POST] Booking created successfully",
                    HttpStatus.CREATED).toBuilder().build();
        } catch (Exception ex) {
            return ResponseUtil.error(
                    "An error occurred while creating booking: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/create/{idRoom}")
    public ResponseEntity<BaseResponseDto<AccommodationBookingDto>> createBookingWithRoom(
            @PathVariable("idRoom") String idRoom,
            @Validated @RequestBody BaseRequestDto<AccommodationBookingCreateRequest> request) {
        try {
            AccommodationBookingDto dto = bookingService.createBookingWithRoom(idRoom, request.getData());
            return ResponseUtil.success(
                    dto,
                    "[POST] Booking created successfully with Room ID: " + idRoom,
                    HttpStatus.CREATED).toBuilder().build();
        } catch (Exception ex) {
            return ResponseUtil.error(
                    "An error occurred while creating booking: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<BaseResponseDto<AccommodationBookingDto>> updateBooking(
            @Validated @RequestBody BaseRequestDto<AccommodationBookingUpdateRequest> request) {
        try {
            AccommodationBookingDto dto = bookingService.updateBooking(
                    request.getData().getBookingId(),
                    request.getData());
            return ResponseUtil.success(
                    dto,
                    "[PUT] Booking with ID: " + request.getData().getBookingId() + " updated successfully",
                    HttpStatus.OK).toBuilder().build();
        } catch (Exception ex) {
            return ResponseUtil.error(
                    "An error occurred while updating booking: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/status/pay")
    public ResponseEntity<BaseResponseDto<AccommodationBookingDto>> markBookingAsPaid(
            @Validated @RequestBody BaseRequestDto<AccommodationBookingPayRequest> request) {
        try {
            AccommodationBookingDto dto = bookingService.markBookingAsPaid(request.getData().getBookingId());
            return ResponseUtil.success(
                    dto,
                    "[POST] Booking with ID: " + request.getData().getBookingId() + " marked as paid successfully",
                    HttpStatus.OK).toBuilder().build();
        } catch (Exception ex) {
            return ResponseUtil.error(
                    "An error occurred while marking booking as paid: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/status/cancel")
    public ResponseEntity<BaseResponseDto<AccommodationBookingDto>> cancelBooking(
            @Validated @RequestBody BaseRequestDto<AccommodationBookingCancelRequest> request) {
        try {
            AccommodationBookingDto dto = bookingService.cancelBooking(request.getData().getBookingId());
            return ResponseUtil.success(
                    dto,
                    "[POST] Booking with ID: " + request.getData().getBookingId() + " cancelled successfully",
                    HttpStatus.OK).toBuilder().build();
        } catch (Exception ex) {
            return ResponseUtil.error(
                    "An error occurred while cancelling booking: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/status/refund")
    public ResponseEntity<BaseResponseDto<AccommodationBookingDto>> refundBooking(
            @Validated @RequestBody BaseRequestDto<AccommodationBookingRefundRequest> request) {
        try {
            AccommodationBookingDto dto = bookingService.refundBooking(request.getData().getBookingId());
            return ResponseUtil.success(
                    dto,
                    "[POST] Booking with ID: " + request.getData().getBookingId() + " refunded successfully",
                    HttpStatus.OK).toBuilder().build();
        } catch (Exception ex) {
            return ResponseUtil.error(
                    "An error occurred while refunding booking: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/chart")
    public ResponseEntity<BaseResponseDto<Map<String, Object>>> getBookingChart(
            @RequestParam(value = "month", required = true) Integer month,
            @RequestParam(value = "year", required = true) Integer year) {
        try {
            Map<String, Object> chartData = bookingService.getBookingChart(month, year);
            return ResponseUtil.success(
                    chartData,
                    "[GET] Booking chart data retrieved successfully for " + month + "/" + year,
                    HttpStatus.OK).toBuilder().build();
        } catch (Exception ex) {
            return ResponseUtil.error(
                    "An error occurred while fetching booking chart: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/customers")
    public ResponseEntity<BaseResponseDto<java.util.List<CustomerSummaryResponseDTO>>> listCustomers() {
        try {
            var customers = bookingService.getCustomers();
            return ResponseUtil.success(customers, "[GET] Customers retrieved successfully", HttpStatus.OK)
                    .toBuilder()
                    .build();
        } catch (Exception ex) {
            return ResponseUtil.error("An error occurred while fetching customers: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
