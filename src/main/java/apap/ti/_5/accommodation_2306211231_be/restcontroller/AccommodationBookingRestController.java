package apap.ti._5.accommodation_2306211231_be.restcontroller;

import apap.ti._5.accommodation_2306211231_be.restdto.BaseRequestDto;
import apap.ti._5.accommodation_2306211231_be.restdto.BaseResponseDto;
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingUpdateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.response.accommodationbooking.AccommodationBookingDto;
import apap.ti._5.accommodation_2306211231_be.restservice.AccommodationBookingRestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class AccommodationBookingRestController {

    private final AccommodationBookingRestService bookingService;

    // Stubs only – service methods will be implemented later
    @GetMapping
    public ResponseEntity<BaseResponseDto<java.util.List<AccommodationBookingDto>>> listBookings() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponseDto<AccommodationBookingDto>> getBooking(@PathVariable("id") String id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PostMapping
    public ResponseEntity<BaseResponseDto<AccommodationBookingDto>> createBooking(
            @Validated @RequestBody BaseRequestDto<AccommodationBookingCreateRequest> request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponseDto<AccommodationBookingDto>> updateBooking(
            @PathVariable("id") String id,
            @Validated @RequestBody BaseRequestDto<AccommodationBookingUpdateRequest> request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
