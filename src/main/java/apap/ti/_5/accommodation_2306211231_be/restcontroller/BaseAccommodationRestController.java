package apap.ti._5.accommodation_2306211231_be.restcontroller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import apap.ti._5.accommodation_2306211231_be.restdto.BaseResponseDto;
import apap.ti._5.accommodation_2306211231_be.restservice.AccommodationBookingRestService;
import apap.ti._5.accommodation_2306211231_be.restservice.PropertyRestService;
import apap.ti._5.accommodation_2306211231_be.util.ResponseUtil;
import apap.ti._5.accommodation_2306211231_be.service.ProvinceService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class BaseAccommodationRestController {
    
    private final PropertyRestService propertyService;
    private final AccommodationBookingRestService bookingService;
    private final ProvinceService provinceService;

    @GetMapping("/")
    public ResponseEntity<BaseResponseDto<Map<String, Long>>> home() {
        try{
            long totalProperties = propertyService.count();
            long totalBookings = bookingService.count();
            return ResponseUtil.<Map<String, Long>>success(
                Map.of(
                    "totalProperties", totalProperties,
                    "totalBookings", totalBookings
                ),
                "[GET] Welcome to Travel APAP Accommodation Service API",
                HttpStatus.OK
            ).toBuilder().build();

        } catch (Exception e) {
            return ResponseUtil.<Map<String, Long>>error(
                "An error occurred while fetching summary data.",
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/external/province")
    public ResponseEntity<BaseResponseDto<Map<Integer, String>>> getProvinces() {
        try {
            Map<Integer, String> provinces = provinceService.getAll();
            return ResponseUtil.<Map<Integer, String>>success(
                provinces,
                "[GET] List of provinces fetched successfully.",
                HttpStatus.OK
            ).toBuilder().build();
        } catch (Exception e) {
            return ResponseUtil.<Map<Integer, String>>error(
                "An error occurred while fetching provinces.",
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}
