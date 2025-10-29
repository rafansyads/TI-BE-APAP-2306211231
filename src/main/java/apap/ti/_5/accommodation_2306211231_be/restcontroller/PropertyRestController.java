package apap.ti._5.accommodation_2306211231_be.restcontroller;

import apap.ti._5.accommodation_2306211231_be.restservice.PropertyRestService;
import apap.ti._5.accommodation_2306211231_be.restdto.BaseRequestDto;
import apap.ti._5.accommodation_2306211231_be.restdto.BaseResponseDto;
import apap.ti._5.accommodation_2306211231_be.restdto.request.property.PropertyCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.property.PropertyUpdateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.response.property.PropertyDetailDto;
import apap.ti._5.accommodation_2306211231_be.restdto.response.property.PropertySummaryDto;
import apap.ti._5.accommodation_2306211231_be.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.*;

@RestController
@RequestMapping("/property")
@RequiredArgsConstructor
public class PropertyRestController {

    private final PropertyRestService propertyService;

    /**
     * List all properties
     * @return ResponseEntity with list of PropertySummaryDto
     * @exception Exception when any error occurs
     */
    @GetMapping
    public ResponseEntity<BaseResponseDto<List<PropertySummaryDto>>> listProperties() {
        try {
            List<PropertySummaryDto> properties = propertyService.getAllPropertiesDto();
            if (properties == null || properties.isEmpty()) {
                return ResponseUtil.success(
                        new ArrayList<PropertySummaryDto>(),
                        "[GET] No properties found",
                        HttpStatus.OK);
            }
            return ResponseUtil.success(
                    properties,
                    "[GET] All properties retrieved successfully with total count: " + properties.size(),
                    HttpStatus.OK);
        } catch (Exception ex) {
            return ResponseUtil.error(
                    "An error occurred while fetching properties: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponseDto<PropertyDetailDto>> getProperty(@PathVariable("id") String id) {
        try {
            PropertyDetailDto dto = propertyService.getPropertyDetailDto(id);
            return ResponseUtil.success(
                    dto,
                    "[GET] The property details retrieved successfully",
                    HttpStatus.OK);
        } catch (IllegalArgumentException ex) {
            return ResponseUtil.error(ex.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<BaseResponseDto<PropertyDetailDto>> createProperty(
            @Validated @RequestBody BaseRequestDto<PropertyCreateRequest> request) {
        try {
            PropertyDetailDto dto = propertyService.createProperty(request.getData());
            return ResponseUtil.success(
                    dto,
                    "[POST] The property details created successfully",
                    HttpStatus.CREATED);
        } catch (IllegalArgumentException ex) {
            return ResponseUtil.error(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<BaseResponseDto<PropertyDetailDto>> updateProperty(
            @Validated @RequestBody BaseRequestDto<PropertyUpdateRequest> request) {
        try {
            String propertyId = request.getData().getPropertyId();
            PropertyDetailDto dto = propertyService.updateProperty(propertyId, request.getData());
            return ResponseUtil.success(
                    dto,
                    "[PUT] The property details updated successfully",
                    HttpStatus.OK);
        } catch (IllegalArgumentException ex) {
            return ResponseUtil.error(
                    ex.getMessage(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<BaseResponseDto<Void>> softDeleteProperty(@PathVariable("id") String id) {
        try {
            propertyService.softDeleteProperty(id);
            return ResponseUtil.success(
                null, 
                "[DELETE] The property details marked as deleted successfully (soft-delete)", 
                HttpStatus.OK);
        } catch (IllegalArgumentException ex) {
            return ResponseUtil.error(
                ex.getMessage(), 
                HttpStatus.NOT_FOUND);
        }
    }
}
