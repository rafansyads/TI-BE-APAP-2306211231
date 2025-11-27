package apap.ti._5.accommodation_2306211231_be.restcontroller;

import apap.ti._5.accommodation_2306211231_be.restservice.PropertyRestService;
import apap.ti._5.accommodation_2306211231_be.restservice.AuthRestService;
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
import apap.ti._5.accommodation_2306211231_be.restdto.response.room.roomtype.RoomTypeDetailDto;
import apap.ti._5.accommodation_2306211231_be.util.ResponseUtil;
import apap.ti._5.accommodation_2306211231_be.util.IdUtil;
import apap.ti._5.accommodation_2306211231_be.restservice.AccommodationReviewRestService;
import apap.ti._5.accommodation_2306211231_be.restmapper.AccommodationReviewMapper;
import apap.ti._5.accommodation_2306211231_be.restmapper.PropertyMapper;
import apap.ti._5.accommodation_2306211231_be.restdto.response.accommodationbooking.AccommodationReviewDTO;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

@RestController
@RequestMapping("/property")
@RequiredArgsConstructor
public class PropertyRestController {

    private final PropertyRestService propertyService;
    private final AuthRestService authRestService;
    private final AccommodationReviewRestService reviewService;

    /**
     * List all properties
     * 
     * @return ResponseEntity with list of PropertySummaryDto
     * @exception Exception when any error occurs
     */
    @GetMapping
    public ResponseEntity<BaseResponseDto<ArrayList<PropertySummaryDto>>> listProperties(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "type", required = false) Integer type,
            @RequestParam(name = "province", required = false) String province) {
        try {
            // determine caller role and username
            var auth = SecurityContextHolder.getContext().getAuthentication();
            String caller = (auth != null) ? auth.getName() : null;
            boolean isOwner = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ACCOMMODATION_OWNER")
                            || a.getAuthority().equals("ROLE_ACCOMMODATION_OWNER"));

            List<PropertySummaryDto> list;
            if (isOwner && caller != null) {
                // owner: only show properties owned by this user
                var user = authRestService.findAggregateByUsername(caller);
                if (user == null)
                    return ResponseUtil.error("Owner not found", HttpStatus.NOT_FOUND);
                var props = propertyService.getAllProperties();
                List<PropertySummaryDto> filtered = new ArrayList<>();
                for (var p : props) {
                    if (p.getOwnerId() != null && p.getOwnerId().equals(user.getId())) {
                        filtered.add(PropertyMapper.toSummaryDto(p));
                    }
                }
                list = filtered;
            } else {
                list = propertyService.getAllPropertiesDto();
            }
            ArrayList<PropertySummaryDto> properties = (list == null) ? new ArrayList<>() : new ArrayList<>(list);

            // apply optional filters
            if (name != null && !name.isBlank()) {
                properties.removeIf(p -> p.getPropertyName() == null
                        || !p.getPropertyName().toLowerCase().contains(name.toLowerCase()));
            }
            if (type != null) {
                properties.removeIf(p -> p.getType() == null || !p.getType().equals(type));
            }
            if (province != null && !province.isBlank()) {
                try {
                    int prov = Integer.parseInt(province);
                    properties.removeIf(p -> p.getProvince() == null || !p.getProvince().equals(prov));
                } catch (NumberFormatException nfe) {
                    // if province isn't numeric, compare against provinceName
                    properties.removeIf(p -> p.getProvinceName() == null
                            || !p.getProvinceName().toLowerCase().contains(province.toLowerCase()));
                }
            }

            if (properties.isEmpty()) {
                return ResponseUtil.success(
                        properties,
                        "[GET] No properties found",
                        HttpStatus.OK).toBuilder().build();
            }

            return ResponseUtil.success(
                    properties,
                    "[GET] All properties retrieved successfully with total count: " + properties.size(),
                    HttpStatus.OK).toBuilder().build();
        } catch (Exception ex) {
            return ResponseUtil.error(
                    "An error occurred while fetching properties: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/reviews")
    public ResponseEntity<BaseResponseDto<List<AccommodationReviewDTO>>> listReviewsByProperty(
            @RequestParam("propertyId") String propertyId) {
        try {
            // basic validations
            if (propertyId == null || propertyId.isBlank()) {
                return ResponseUtil.error("propertyId is required", HttpStatus.BAD_REQUEST);
            }

            var auth = SecurityContextHolder.getContext().getAuthentication();
            String caller = (auth != null) ? auth.getName() : null;
            boolean isOwner = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ACCOMMODATION_OWNER")
                            || a.getAuthority().equals("ROLE_ACCOMMODATION_OWNER"));
            boolean isSuper = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("SUPERADMIN") || a.getAuthority().equals("ROLE_SUPERADMIN"));

            if (isOwner && caller != null && !isSuper) {
                // verify owner owns the property, superadmin can bypass
                var propertyDto = propertyService.getPropertyDetailDto(propertyId);
                var callerAgg = authRestService.findAggregateByUsername(caller);
                String ownerId = propertyDto.getOwnerId();
                if (callerAgg == null || ownerId == null || !ownerId.equals(callerAgg.getId() == null ? null : callerAgg.getId().toString())) {
                    return ResponseUtil.error("Forbidden", HttpStatus.FORBIDDEN);
                }
            }

            var reviews = reviewService.findByPropertyId(propertyId);
            var dtos = reviews.stream().map(AccommodationReviewMapper::toDTO).toList();
            return ResponseUtil.success(dtos, "[GET] Reviews for property retrieved", HttpStatus.OK).toBuilder().build();
        } catch (IllegalArgumentException ex) {
            return ResponseUtil.error(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            return ResponseUtil.error("An error occurred while fetching reviews: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Helper endpoint to predict next propertyId (and sequence) before creating a
     * property.
     * Clients can use this to pre-compute roomTypeId (<SEQ>-<name>-<floor>) when
     * multiple room types exist.
     *
     * Example: GET /api/property/predict?type=1&ownerId=<uuid>
     */
    @GetMapping("/predict")
    public ResponseEntity<BaseResponseDto<Map<String, Object>>> predictPropertyId(
            @RequestParam("type") int type,
            @RequestParam("ownerId") String ownerId) {
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
                    HttpStatus.OK).toBuilder().build();
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

            // Owner can only see details of their own property
            var auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isOwner = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ACCOMMODATION_OWNER")
                            || a.getAuthority().equals("ROLE_ACCOMMODATION_OWNER"));
            if (isOwner && auth != null) {
                var user = authRestService.findAggregateByUsername(auth.getName());
                if (user == null)
                    return ResponseUtil.error("Owner not found", HttpStatus.NOT_FOUND);
                // dto.getOwnerId() is String (UUID as string), user.getId() is UUID -> compare string forms
                String callerId = (user.getId() == null) ? null : user.getId().toString();
                if (dto == null || dto.getOwnerId() == null || !dto.getOwnerId().equals(callerId)) {
                    return ResponseUtil.error("Forbidden", HttpStatus.FORBIDDEN);
                }
            }
            return ResponseUtil.success(
                    dto,
                    "[GET] The property details retrieved successfully"
                            + (checkIn != null && checkOut != null ? " with availability filter" : ""),
                    HttpStatus.OK).toBuilder().build();
        } catch (IllegalArgumentException ex) {
            return ResponseUtil.error(ex.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{propertyId}/roomtype/{id}")
    public ResponseEntity<BaseResponseDto<RoomTypeDetailDto>> getRoomTypeById(
            @PathVariable("propertyId") String propertyId,
            @PathVariable("id") String id,
            @RequestParam(name = "checkIn", required = false) String checkIn,
            @RequestParam(name = "checkOut", required = false) String checkOut
        ) {
        try {
            // pass date filters to service so availability can be computed per-room
            RoomTypeDetailDto dto = propertyService.getRoomTypeById(propertyId, id, checkIn, checkOut);

            // Authorization: mirror getProperty checks — owners can only access their own property's room types
            var auth = SecurityContextHolder.getContext().getAuthentication();
            String caller = (auth != null) ? auth.getName() : null;
            boolean isOwner = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ACCOMMODATION_OWNER")
                            || a.getAuthority().equals("ROLE_ACCOMMODATION_OWNER"));
            boolean isSuper = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("SUPERADMIN") || a.getAuthority().equals("ROLE_SUPERADMIN"));

            if (isOwner && caller != null && !isSuper) {
                // Ensure the owner only accesses their own property's room types
                PropertyDetailDto pdto = propertyService.getPropertyDetailDto(propertyId);
                if (pdto == null
                        || (pdto.getOwnerName() != null && !pdto.getOwnerName().equals(caller)
                            && (pdto.getOwnerId() == null || !pdto.getOwnerId().toString().equals(caller)))) {
                    return ResponseUtil.error("Forbidden: owner cannot access this property", HttpStatus.FORBIDDEN);
                }
            }
            return ResponseUtil.success(
                    dto,
                    "[GET] The room types for the property retrieved successfully",
                    HttpStatus.OK).toBuilder().build();
        } catch (IllegalArgumentException ex) {
            return ResponseUtil.error(ex.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<BaseResponseDto<PropertyDetailDto>> createProperty(
            @Validated @RequestBody BaseRequestDto<PropertyCreateRequest> request) {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            String caller = (auth != null) ? auth.getName() : null;
            boolean isOwner = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ACCOMMODATION_OWNER")
                            || a.getAuthority().equals("ROLE_ACCOMMODATION_OWNER"));
            boolean isSuper = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("SUPERADMIN") || a.getAuthority().equals("ROLE_SUPERADMIN"));

            // If superadmin: payload must include ownerId
            if (isSuper) {
                if (request.getData() == null || request.getData().getOwnerId() == null
                        || request.getData().getOwnerId().isBlank()) {
                    return ResponseUtil.error("Superadmin must provide ownerId in payload", HttpStatus.BAD_REQUEST);
                }
            }

            // If accommodation owner: set ownerId to caller (cannot assign different owner)
            if (isOwner) {
                var user = authRestService.findAggregateByUsername(caller);
                if (user == null)
                    return ResponseUtil.error("Owner not found", HttpStatus.NOT_FOUND);
                request.getData().setOwnerId(user.getId().toString());
            }

            PropertyDetailDto dto = propertyService.createProperty(request.getData());
            return ResponseUtil.success(
                    dto,
                    "[POST] The property details created successfully",
                    HttpStatus.CREATED).toBuilder().build();
        } catch (IllegalArgumentException ex) {
            return ResponseUtil.error(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<BaseResponseDto<PropertyDetailDto>> updateProperty(
            @Validated @RequestBody BaseRequestDto<PropertyUpdateRequest> request) {
        try {
            String propertyId = request.getData().getPropertyId();

            // if owner role, ensure they own the property
            var auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isOwner = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ACCOMMODATION_OWNER")
                            || a.getAuthority().equals("ROLE_ACCOMMODATION_OWNER"));
            if (isOwner && auth != null) {
                var user = authRestService.findAggregateByUsername(auth.getName());
                if (user == null)
                    return ResponseUtil.error("Owner not found", HttpStatus.NOT_FOUND);
                var existing = propertyService.getPropertyById(propertyId)
                        .orElseThrow(() -> new IllegalArgumentException("Property not found: " + propertyId));
                if (existing.getOwnerId() == null || !existing.getOwnerId().equals(user.getId())) {
                    return ResponseUtil.error("Forbidden", HttpStatus.FORBIDDEN);
                }
            }

            PropertyDetailDto dto = propertyService.updateProperty(propertyId, request.getData());
            return ResponseUtil.success(
                    dto,
                    "[PUT] The property details updated successfully",
                    HttpStatus.OK).toBuilder().build();
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

            // Owner check: if caller is an accommodation owner, ensure they own the property
            var auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isOwner = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ACCOMMODATION_OWNER")
                            || a.getAuthority().equals("ROLE_ACCOMMODATION_OWNER"));
            if (isOwner && auth != null) {
                var user = authRestService.findAggregateByUsername(auth.getName());
                if (user == null)
                    return ResponseUtil.error("Owner not found", HttpStatus.NOT_FOUND);
                var existing = propertyService.getPropertyById(propertyId)
                        .orElseThrow(() -> new IllegalArgumentException("Property not found: " + propertyId));
                if (existing.getOwnerId() == null || !existing.getOwnerId().equals(user.getId())) {
                    return ResponseUtil.error("Forbidden", HttpStatus.FORBIDDEN);
                }
            }

            PropertyDetailDto dto = propertyService.updatePropertyRooms(propertyId, request.getData());
            return ResponseUtil.success(
                    dto,
                    "[POST] The property rooms updated successfully",
                    HttpStatus.OK).toBuilder().build();
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
            // Owner check: ensure caller owns the property for this room (if caller is owner)
            String roomId = request.getData().getId();
            String propertyId = IdUtil.fetchPropertyIdFromRoomId(roomId);
            var auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isOwner = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ACCOMMODATION_OWNER")
                            || a.getAuthority().equals("ROLE_ACCOMMODATION_OWNER"));
            if (isOwner && auth != null) {
                var user = authRestService.findAggregateByUsername(auth.getName());
                if (user == null)
                    return ResponseUtil.error("Owner not found", HttpStatus.NOT_FOUND);
                var existing = propertyService.getPropertyById(propertyId)
                        .orElseThrow(() -> new IllegalArgumentException("Property not found: " + propertyId));
                if (existing.getOwnerId() == null || !existing.getOwnerId().equals(user.getId())) {
                    return ResponseUtil.error("Forbidden", HttpStatus.FORBIDDEN);
                }
            }

            RoomDetailDto dto = propertyService.addMaintenance(request.getData());
            return ResponseUtil.success(
                    dto,
                    "[POST] The maintenance schedule added successfully",
                    HttpStatus.OK).toBuilder().build();
        } catch (IllegalArgumentException ex) {
            return ResponseUtil.error(
                    ex.getMessage(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Recompute and persist totalRoom for a property by counting all rooms under
     * it.
     */
    @PostMapping("/recompute-totalrooms/{id}")
    public ResponseEntity<BaseResponseDto<PropertyDetailDto>> recomputeTotalRooms(@PathVariable("id") String id) {
        try {
            // Owner check: only property owner or superadmin may recompute
            var auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isOwner = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ACCOMMODATION_OWNER")
                            || a.getAuthority().equals("ROLE_ACCOMMODATION_OWNER"));
            boolean isSuper = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("SUPERADMIN") || a.getAuthority().equals("ROLE_SUPERADMIN"));
            if (isOwner && auth != null && !isSuper) {
                var user = authRestService.findAggregateByUsername(auth.getName());
                if (user == null)
                    return ResponseUtil.error("Owner not found", HttpStatus.NOT_FOUND);
                var existing = propertyService.getPropertyById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Property not found: " + id));
                if (existing.getOwnerId() == null || !existing.getOwnerId().equals(user.getId())) {
                    return ResponseUtil.error("Forbidden", HttpStatus.FORBIDDEN);
                }
            }

            PropertyDetailDto dto = propertyService.recomputeTotalRooms(id);
            return ResponseUtil.success(dto, "[POST] Recomputed totalRoom successfully", HttpStatus.OK).toBuilder()
                    .build();
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
            return ResponseUtil.success(owners, "[GET] Owners retrieved successfully", HttpStatus.OK).toBuilder()
                    .build();
        } catch (Exception ex) {
            return ResponseUtil.error("Failed to fetch owners", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<BaseResponseDto<Void>> softDeleteProperty(@PathVariable("id") String id) {
        try {
            // owner check
            var auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isOwner = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ACCOMMODATION_OWNER")
                            || a.getAuthority().equals("ROLE_ACCOMMODATION_OWNER"));
            if (isOwner && auth != null) {
                var user = authRestService.findAggregateByUsername(auth.getName());
                if (user == null)
                    return ResponseUtil.error("Owner not found", HttpStatus.NOT_FOUND);
                var existing = propertyService.getPropertyById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Property not found: " + id));
                if (existing.getOwnerId() == null || !existing.getOwnerId().equals(user.getId())) {
                    return ResponseUtil.error("Forbidden", HttpStatus.FORBIDDEN);
                }
            }

            propertyService.softDeleteProperty(id);
            return ResponseUtil.<Void>success(
                    null,
                    "[DELETE] The property details marked as deleted successfully (soft-delete)",
                    HttpStatus.OK).toBuilder().build();
        } catch (IllegalArgumentException ex) {
            return ResponseUtil.error(
                    ex.getMessage(),
                    HttpStatus.NOT_FOUND);
        }
    }
}
