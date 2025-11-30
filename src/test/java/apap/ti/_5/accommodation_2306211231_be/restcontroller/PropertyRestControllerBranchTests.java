package apap.ti._5.accommodation_2306211231_be.restcontroller;

import apap.ti._5.accommodation_2306211231_be.models.AccommodationReview;
import apap.ti._5.accommodation_2306211231_be.models.Property;
import apap.ti._5.accommodation_2306211231_be.models.profile.AccommodationOwner;
import apap.ti._5.accommodation_2306211231_be.models.profile.Customer;
import apap.ti._5.accommodation_2306211231_be.restdto.BaseRequestDto;
import apap.ti._5.accommodation_2306211231_be.restdto.BaseResponseDto;
import apap.ti._5.accommodation_2306211231_be.restdto.request.property.*;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.*;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.roomtype.*;
import apap.ti._5.accommodation_2306211231_be.restdto.response.property.*;
import apap.ti._5.accommodation_2306211231_be.restdto.response.room.*;
import apap.ti._5.accommodation_2306211231_be.restdto.response.room.roomtype.*;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PropertyRestControllerBranchTests {

    @Mock private PropertyRestService propertyService;
    @Mock private AuthRestService authRestService;
    @Mock private AccommodationReviewRestService reviewService;

    private PropertyRestController controller;
    private SecurityContext originalContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new PropertyRestController(propertyService, authRestService, reviewService);
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
    void listProperties_ownerNotFound_returns404() {
        setAuthentication("owner1", "ACCOMMODATION_OWNER");
        when(authRestService.findAggregateByUsername("owner1")).thenReturn(null);

        ResponseEntity<BaseResponseDto<ArrayList<PropertySummaryDto>>> response = 
            controller.listProperties(null, null, null);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void listProperties_ownerWithFilters_appliesFilters() {
        setAuthentication("owner1", "ACCOMMODATION_OWNER");
        
        AccommodationOwner owner = new AccommodationOwner();
        owner.setId(UUID.randomUUID());
        when(authRestService.findAggregateByUsername("owner1")).thenReturn(owner);
        
        Property prop = new Property();
        prop.setPropertyId("P001");
        prop.setPropertyName("Test Hotel");
        prop.setOwnerId(owner.getId());
        prop.setType(1);
        prop.setProvince(1);
        
        when(propertyService.getAllProperties()).thenReturn(List.of(prop));

        ResponseEntity<BaseResponseDto<ArrayList<PropertySummaryDto>>> response = 
            controller.listProperties("Test", 1, "1");

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void listProperties_provinceAsName_filtersCorrectly() {
        setAuthentication("admin", "SUPERADMIN");
        
        PropertySummaryDto dto = new PropertySummaryDto();
        dto.setPropertyName("Hotel");
        dto.setProvinceName("Jakarta");
        
        when(propertyService.getAllPropertiesDto()).thenReturn(List.of(dto));

        ResponseEntity<BaseResponseDto<ArrayList<PropertySummaryDto>>> response = 
            controller.listProperties(null, null, "Jakarta");

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void listProperties_exception_returns500() {
        setAuthentication("admin", "SUPERADMIN");
        when(propertyService.getAllPropertiesDto()).thenThrow(new RuntimeException("Error"));

        ResponseEntity<BaseResponseDto<ArrayList<PropertySummaryDto>>> response = 
            controller.listProperties(null, null, null);

        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    void listReviewsByProperty_blankPropertyId_returns400() {
        ResponseEntity<BaseResponseDto<List<AccommodationReviewDTO>>> response = 
            controller.listReviewsByProperty("   ");

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void listReviewsByProperty_ownerNotOwning_returnsForbidden() {
        setAuthentication("owner1", "ACCOMMODATION_OWNER");
        
        AccommodationOwner owner = new AccommodationOwner();
        owner.setId(UUID.randomUUID());
        when(authRestService.findAggregateByUsername("owner1")).thenReturn(owner);
        
        PropertyDetailDto propDto = new PropertyDetailDto();
        propDto.setOwnerId(UUID.randomUUID().toString()); // Different owner
        when(propertyService.getPropertyDetailDto("P001")).thenReturn(propDto);

        ResponseEntity<BaseResponseDto<List<AccommodationReviewDTO>>> response = 
            controller.listReviewsByProperty("P001");

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void listReviewsByProperty_exception_returns500() {
        setAuthentication("admin", "SUPERADMIN");
        when(reviewService.findByPropertyId("P001")).thenThrow(new RuntimeException("Error"));

        ResponseEntity<BaseResponseDto<List<AccommodationReviewDTO>>> response = 
            controller.listReviewsByProperty("P001");

        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    void predictPropertyId_invalidOwnerId_returns400() {
        ResponseEntity<BaseResponseDto<Map<String, Object>>> response = 
            controller.predictPropertyId(1, "invalid-uuid");

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void predictPropertyId_exception_returns500() {
        when(propertyService.predictNextPropertySequence()).thenThrow(new RuntimeException("Error"));

        ResponseEntity<BaseResponseDto<Map<String, Object>>> response = 
            controller.predictPropertyId(1, UUID.randomUUID().toString());

        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    void getProperty_ownerNotFound_returns404() {
        setAuthentication("owner1", "ACCOMMODATION_OWNER");
        
        PropertyDetailDto dto = new PropertyDetailDto();
        dto.setOwnerId(UUID.randomUUID().toString());
        when(propertyService.getPropertyDetailDto("P001")).thenReturn(dto);
        when(authRestService.findAggregateByUsername("owner1")).thenReturn(null);

        ResponseEntity<BaseResponseDto<PropertyDetailDto>> response = 
            controller.getProperty("P001", null, null);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void getProperty_ownerAccessingOtherProperty_returnsForbidden() {
        setAuthentication("owner1", "ACCOMMODATION_OWNER");
        
        AccommodationOwner owner = new AccommodationOwner();
        owner.setId(UUID.randomUUID());
        when(authRestService.findAggregateByUsername("owner1")).thenReturn(owner);
        
        PropertyDetailDto dto = new PropertyDetailDto();
        dto.setOwnerId(UUID.randomUUID().toString()); // Different owner
        when(propertyService.getPropertyDetailDto("P001")).thenReturn(dto);

        ResponseEntity<BaseResponseDto<PropertyDetailDto>> response = 
            controller.getProperty("P001", null, null);

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void getProperty_withDateFilters_callsWithFilters() {
        setAuthentication("admin", "SUPERADMIN");
        
        PropertyDetailDto dto = new PropertyDetailDto();
        when(propertyService.getPropertyDetailDto("P001", "2024-01-01", "2024-01-05")).thenReturn(dto);

        ResponseEntity<BaseResponseDto<PropertyDetailDto>> response = 
            controller.getProperty("P001", "2024-01-01", "2024-01-05");

        assertEquals(200, response.getStatusCode().value());
        verify(propertyService).getPropertyDetailDto("P001", "2024-01-01", "2024-01-05");
    }

    @Test
    void getProperty_notFound_returns404() {
        setAuthentication("admin", "SUPERADMIN");
        when(propertyService.getPropertyDetailDto("P001")).thenThrow(new IllegalArgumentException("Not found"));

        ResponseEntity<BaseResponseDto<PropertyDetailDto>> response = 
            controller.getProperty("P001", null, null);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void getRoomTypeById_ownerNotFound_returns404() {
        setAuthentication("owner1", "ACCOMMODATION_OWNER");
        
        RoomTypeDetailDto rtDto = new RoomTypeDetailDto();
        when(propertyService.getRoomTypeById("P001", "RT001", null, null)).thenReturn(rtDto);
        when(authRestService.findAggregateByUsername("owner1")).thenReturn(null);

        ResponseEntity<BaseResponseDto<RoomTypeDetailDto>> response = 
            controller.getRoomTypeById("P001", "RT001", null, null);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void getRoomTypeById_ownerAccessingOtherProperty_returnsForbidden() {
        setAuthentication("owner1", "ACCOMMODATION_OWNER");
        
        AccommodationOwner owner = new AccommodationOwner();
        owner.setId(UUID.randomUUID());
        when(authRestService.findAggregateByUsername("owner1")).thenReturn(owner);
        
        RoomTypeDetailDto rtDto = new RoomTypeDetailDto();
        when(propertyService.getRoomTypeById("P001", "RT001", null, null)).thenReturn(rtDto);
        
        PropertyDetailDto propDto = new PropertyDetailDto();
        propDto.setOwnerId(UUID.randomUUID().toString()); // Different owner
        when(propertyService.getPropertyDetailDto("P001")).thenReturn(propDto);

        ResponseEntity<BaseResponseDto<RoomTypeDetailDto>> response = 
            controller.getRoomTypeById("P001", "RT001", null, null);

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void createProperty_superadminMissingOwnerId_returns400() {
        setAuthentication("admin", "SUPERADMIN");
        
        PropertyCreateRequest data = new PropertyCreateRequest();
        BaseRequestDto<PropertyCreateRequest> req = new BaseRequestDto<>();
        req.setData(data);

        ResponseEntity<BaseResponseDto<PropertyDetailDto>> response = controller.createProperty(req);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void createProperty_ownerNotFound_returns404() {
        setAuthentication("owner1", "ACCOMMODATION_OWNER");
        when(authRestService.findAggregateByUsername("owner1")).thenReturn(null);
        
        PropertyCreateRequest data = new PropertyCreateRequest();
        BaseRequestDto<PropertyCreateRequest> req = new BaseRequestDto<>();
        req.setData(data);

        ResponseEntity<BaseResponseDto<PropertyDetailDto>> response = controller.createProperty(req);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void createProperty_validationError_returns400() {
        setAuthentication("admin", "SUPERADMIN");
        
        PropertyCreateRequest data = new PropertyCreateRequest();
        data.setOwnerId(UUID.randomUUID().toString());
        BaseRequestDto<PropertyCreateRequest> req = new BaseRequestDto<>();
        req.setData(data);
        
        when(propertyService.createProperty(any(PropertyCreateRequest.class))).thenThrow(new IllegalArgumentException("Invalid"));

        ResponseEntity<BaseResponseDto<PropertyDetailDto>> response = controller.createProperty(req);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void updateProperty_ownerNotFound_returns404() {
        setAuthentication("owner1", "ACCOMMODATION_OWNER");
        when(authRestService.findAggregateByUsername("owner1")).thenReturn(null);
        
        PropertyUpdateRequest data = new PropertyUpdateRequest();
        data.setPropertyId("P001");
        BaseRequestDto<PropertyUpdateRequest> req = new BaseRequestDto<>();
        req.setData(data);

        ResponseEntity<BaseResponseDto<PropertyDetailDto>> response = controller.updateProperty(req);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void updateProperty_ownerNotOwning_returnsForbidden() {
        setAuthentication("owner1", "ACCOMMODATION_OWNER");
        
        AccommodationOwner owner = new AccommodationOwner();
        owner.setId(UUID.randomUUID());
        when(authRestService.findAggregateByUsername("owner1")).thenReturn(owner);
        
        Property prop = new Property();
        prop.setOwnerId(UUID.randomUUID()); // Different owner
        when(propertyService.getPropertyById("P001")).thenReturn(Optional.of(prop));
        
        PropertyUpdateRequest data = new PropertyUpdateRequest();
        data.setPropertyId("P001");
        BaseRequestDto<PropertyUpdateRequest> req = new BaseRequestDto<>();
        req.setData(data);

        ResponseEntity<BaseResponseDto<PropertyDetailDto>> response = controller.updateProperty(req);

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void updatePropertyRooms_ownerNotOwning_returnsForbidden() {
        setAuthentication("owner1", "ACCOMMODATION_OWNER");
        
        AccommodationOwner owner = new AccommodationOwner();
        owner.setId(UUID.randomUUID());
        when(authRestService.findAggregateByUsername("owner1")).thenReturn(owner);
        
        Property prop = new Property();
        prop.setOwnerId(UUID.randomUUID()); // Different owner
        when(propertyService.getPropertyById("P001")).thenReturn(Optional.of(prop));
        
        RoomTypeCreateRequest data = new RoomTypeCreateRequest();
        data.setPropertyId("P001");
        BaseRequestDto<RoomTypeCreateRequest> req = new BaseRequestDto<>();
        req.setData(data);

        ResponseEntity<BaseResponseDto<PropertyDetailDto>> response = controller.updatePropertyRooms(req);

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void addMaintenance_ownerNotOwning_returnsForbidden() {
        setAuthentication("owner1", "ACCOMMODATION_OWNER");
        
        AccommodationOwner owner = new AccommodationOwner();
        owner.setId(UUID.randomUUID());
        when(authRestService.findAggregateByUsername("owner1")).thenReturn(owner);
        
        Property prop = new Property();
        prop.setOwnerId(UUID.randomUUID()); // Different owner
        when(propertyService.getPropertyById(anyString())).thenReturn(Optional.of(prop));
        
        RoomUpdateRequest data = new RoomUpdateRequest();
        data.setId("1-P001-RT001-R001");
        BaseRequestDto<RoomUpdateRequest> req = new BaseRequestDto<>();
        req.setData(data);

        ResponseEntity<BaseResponseDto<RoomDetailDto>> response = controller.addMaintenance(req);

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void recomputeTotalRooms_ownerNotOwning_returnsForbidden() {
        setAuthentication("owner1", "ACCOMMODATION_OWNER");
        
        AccommodationOwner owner = new AccommodationOwner();
        owner.setId(UUID.randomUUID());
        when(authRestService.findAggregateByUsername("owner1")).thenReturn(owner);
        
        Property prop = new Property();
        prop.setOwnerId(UUID.randomUUID()); // Different owner
        when(propertyService.getPropertyById("P001")).thenReturn(Optional.of(prop));

        ResponseEntity<BaseResponseDto<PropertyDetailDto>> response = controller.recomputeTotalRooms("P001");

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void listOwners_exception_returns500() {
        when(propertyService.getOwners()).thenThrow(new RuntimeException("Error"));

        ResponseEntity<BaseResponseDto<List<OwnerSummaryDto>>> response = controller.listOwners();

        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    void softDeleteProperty_ownerNotOwning_returnsForbidden() {
        setAuthentication("owner1", "ACCOMMODATION_OWNER");
        
        AccommodationOwner owner = new AccommodationOwner();
        owner.setId(UUID.randomUUID());
        when(authRestService.findAggregateByUsername("owner1")).thenReturn(owner);
        
        Property prop = new Property();
        prop.setOwnerId(UUID.randomUUID()); // Different owner
        when(propertyService.getPropertyById("P001")).thenReturn(Optional.of(prop));

        ResponseEntity<BaseResponseDto<Void>> response = controller.softDeleteProperty("P001");

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void softDeleteProperty_notFound_returns404() {
        setAuthentication("admin", "SUPERADMIN");
        doThrow(new IllegalArgumentException("Not found")).when(propertyService).softDeleteProperty("P001");

        ResponseEntity<BaseResponseDto<Void>> response = controller.softDeleteProperty("P001");

        assertEquals(404, response.getStatusCode().value());
    }
}
