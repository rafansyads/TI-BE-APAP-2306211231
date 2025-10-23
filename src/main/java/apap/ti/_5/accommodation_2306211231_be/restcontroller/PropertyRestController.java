package apap.ti._5.accommodation_2306211231_be.restcontroller;

import apap.ti._5.accommodation_2306211231_be.restservice.PropertyRestService;
import apap.ti._5.accommodation_2306211231_be.restdto.BaseRequestDto;
import apap.ti._5.accommodation_2306211231_be.restdto.BaseResponseDto;
import apap.ti._5.accommodation_2306211231_be.restdto.request.property.PropertyCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.property.PropertyUpdateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.response.property.PropertyDetailDto;
import apap.ti._5.accommodation_2306211231_be.restdto.response.property.PropertySummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/property")
@RequiredArgsConstructor
public class PropertyRestController {

    private final PropertyRestService propertyService;

    // Stubs only – service methods will be implemented later
    @GetMapping
    public ResponseEntity<BaseResponseDto<java.util.List<PropertySummaryDto>>> listProperties() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponseDto<PropertyDetailDto>> getProperty(@PathVariable("id") String id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PostMapping
    public ResponseEntity<BaseResponseDto<PropertyDetailDto>> createProperty(
            @Validated @RequestBody BaseRequestDto<PropertyCreateRequest> request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponseDto<PropertyDetailDto>> updateProperty(
            @PathVariable("id") String id,
            @Validated @RequestBody BaseRequestDto<PropertyUpdateRequest> request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponseDto<Void>> softDeleteProperty(@PathVariable("id") String id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
