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
import apap.ti._5.accommodation_2306211231_be.util.ResponseUtil;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class AccommodationBookingRestController {

    private final AccommodationBookingRestService bookingService;

    // Stubs only – service methods will be implemented later
    @GetMapping
    public ResponseEntity<BaseResponseDto<List<AccommodationBookingDto>>> listBookings() {
        try {
            List<AccommodationBookingDto> bookings = bookingService.getAllBookingsDto();
            if (bookings == null || bookings.isEmpty()) {
                return ResponseUtil.success(
                        new ArrayList<AccommodationBookingDto>(),
                        "[GET] No bookings found",
                        HttpStatus.OK);
            }
            return ResponseUtil.success(
                    bookings,
                    "[GET] All bookings retrieved successfully with total count: " + bookings.size(),
                    HttpStatus.OK);
        } catch (Exception ex) {
            return ResponseUtil.error(
                    "An error occurred while fetching bookings: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
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
                HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponseDto<AccommodationBookingDto>> getBooking(@PathVariable("id") String id) {
        try {
            AccommodationBookingDto dto = bookingService.getBookingDtoById(id);
            return ResponseUtil.success(
                    dto,
                    "[GET] Booking retrieved successfully for ID: " + id,
                    HttpStatus.OK);
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
                    HttpStatus.CREATED);
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
                    HttpStatus.CREATED);
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
                    HttpStatus.OK);
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
                    HttpStatus.OK);
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
                    HttpStatus.OK);
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
                    HttpStatus.OK);
        } catch (Exception ex) {
            return ResponseUtil.error(
                    "An error occurred while refunding booking: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/chart")
    public ResponseEntity<BaseResponseDto<Map<String, Object>>> getBookingChart(
        @RequestParam(value = "month", required = true) Integer month,
        @RequestParam(value = "year", required = true) Integer year
    ) {
        try {
            Map<String, Object> chartData = bookingService.getBookingChart(month, year);
            return ResponseUtil.success(
                    chartData,
                    "[GET] Booking chart data retrieved successfully for " + month + "/" + year,
                    HttpStatus.OK);
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
            return ResponseUtil.success(customers, "[GET] Customers retrieved successfully", HttpStatus.OK);
        } catch (Exception ex) {
            return ResponseUtil.error("An error occurred while fetching customers: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
