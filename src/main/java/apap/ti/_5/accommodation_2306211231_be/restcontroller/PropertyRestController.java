package apap.ti._5.accommodation_2306211231_be.restcontroller;

import apap.ti._5.accommodation_2306211231_be.restservice.PropertyRestService;
import apap.ti._5.accommodation_2306211231_be.restdto.BaseRequestDto;
import apap.ti._5.accommodation_2306211231_be.restdto.BaseResponseDto;
import apap.ti._5.accommodation_2306211231_be.restdto.request.property.PropertyCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.property.PropertyUpdateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.RoomUpdateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.roomtype.RoomTypeCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.response.property.PropertyDetailDto;
import apap.ti._5.accommodation_2306211231_be.restdto.response.property.PropertySummaryDto;
import apap.ti._5.accommodation_2306211231_be.restdto.response.property.OwnerSummaryDto;
import apap.ti._5.accommodation_2306211231_be.restdto.response.room.RoomDetailDto;
import apap.ti._5.accommodation_2306211231_be.util.ResponseUtil;
import apap.ti._5.accommodation_2306211231_be.util.IdUtil;
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

    /**
     * Helper endpoint to predict next propertyId (and sequence) before creating a property.
     * Clients can use this to pre-compute roomTypeId (<SEQ>-<name>-<floor>) when multiple room types exist.
     *
     * Example: GET /api/property/predict?type=1&ownerId=<uuid>
     */
    @GetMapping("/predict")
    public ResponseEntity<BaseResponseDto<Map<String, Object>>> predictPropertyId(
            @RequestParam("type") int type,
            @RequestParam("ownerId") String ownerId
    ) {
        try {
            UUID ownerUuid = UUID.fromString(ownerId);
            int nextSeq = propertyService.predictNextPropertySequence();
            String predictedPropertyId = IdUtil
                    .generatePropertyId(type, ownerUuid, nextSeq);

            Map<String, Object> payload = new HashMap<>();
            payload.put("nextSequence", nextSeq);
            payload.put("predictedPropertyId", predictedPropertyId);

            return ResponseUtil.success(
                    payload,
                    "Predicted propertyId generated successfully",
                    HttpStatus.OK
            );
        } catch (IllegalArgumentException e) {
            return ResponseUtil.error("Invalid ownerId UUID", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return ResponseUtil.error("Failed to predict propertyId", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponseDto<PropertyDetailDto>> getProperty(
            @PathVariable("id") String id,
            @RequestParam(name = "checkIn", required = false) String checkIn,
            @RequestParam(name = "checkOut", required = false) String checkOut) {
        try {
            PropertyDetailDto dto = (checkIn != null && checkOut != null)
                    ? propertyService.getPropertyDetailDto(id, checkIn, checkOut)
                    : propertyService.getPropertyDetailDto(id);
            return ResponseUtil.success(
                    dto,
                    "[GET] The property details retrieved successfully" + (checkIn != null && checkOut != null ? " with availability filter" : ""),
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

    @PostMapping("/updateroom")
    public ResponseEntity<BaseResponseDto<PropertyDetailDto>> updatePropertyRooms(
            @Validated @RequestBody BaseRequestDto<RoomTypeCreateRequest> request) {
        try {
            String propertyId = request.getData().getPropertyId();
        PropertyDetailDto dto = propertyService.updatePropertyRooms(propertyId, request.getData());
            return ResponseUtil.success(
                    dto,
                    "[POST] The property rooms updated successfully",
                    HttpStatus.OK);
        } catch (IllegalArgumentException ex) {
            return ResponseUtil.error(
                    ex.getMessage(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/maintenance/add")
    public ResponseEntity<BaseResponseDto<RoomDetailDto>> addMaintenance(
            @Validated @RequestBody BaseRequestDto<RoomUpdateRequest> request) {
        try {
            RoomDetailDto dto = propertyService.addMaintenance(request.getData());
            return ResponseUtil.success(
                    dto,
                    "[POST] The maintenance schedule added successfully",
                    HttpStatus.OK);
        } catch (IllegalArgumentException ex) {
            return ResponseUtil.error(
                    ex.getMessage(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Recompute and persist totalRoom for a property by counting all rooms under it.
     */
    @PostMapping("/recompute-totalrooms/{id}")
    public ResponseEntity<BaseResponseDto<PropertyDetailDto>> recomputeTotalRooms(@PathVariable("id") String id) {
        try {
            PropertyDetailDto dto = propertyService.recomputeTotalRooms(id);
            return ResponseUtil.success(dto, "[POST] Recomputed totalRoom successfully", HttpStatus.OK);
        } catch (IllegalArgumentException ex) {
            return ResponseUtil.error(ex.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * List distinct owners (UUID + name) based on existing properties.
     */
    @GetMapping("/owners")
    public ResponseEntity<BaseResponseDto<List<OwnerSummaryDto>>> listOwners() {
        try {
            List<OwnerSummaryDto> owners = propertyService.getOwners();
            return ResponseUtil.success(owners, "[GET] Owners retrieved successfully", HttpStatus.OK);
        } catch (Exception ex) {
            return ResponseUtil.error("Failed to fetch owners", HttpStatus.INTERNAL_SERVER_ERROR);
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
