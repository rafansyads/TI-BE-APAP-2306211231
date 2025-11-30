package apap.ti._5.accommodation_2306211231_be.restcontroller;

import apap.ti._5.accommodation_2306211231_be.models.Property;
import apap.ti._5.accommodation_2306211231_be.models.Room;
import apap.ti._5.accommodation_2306211231_be.models.RoomType;
import apap.ti._5.accommodation_2306211231_be.models.profile.AccommodationOwner;
import apap.ti._5.accommodation_2306211231_be.restdto.BaseResponseDto;
import apap.ti._5.accommodation_2306211231_be.restdto.response.property.PropertyDetailDto;
import apap.ti._5.accommodation_2306211231_be.restdto.response.property.PropertySummaryDto;
import apap.ti._5.accommodation_2306211231_be.restdto.response.accommodationbooking.AccommodationReviewDTO;
import apap.ti._5.accommodation_2306211231_be.restservice.PropertyRestService;
import apap.ti._5.accommodation_2306211231_be.restservice.AuthRestService;
import apap.ti._5.accommodation_2306211231_be.restservice.AccommodationReviewRestService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import apap.ti._5.accommodation_2306211231_be.restdto.BaseRequestDto;
import apap.ti._5.accommodation_2306211231_be.restdto.request.property.PropertyCreateRequest;
import apap.ti._5.accommodation_2306211231_be.models.profile.AccommodationOwner;
import org.mockito.ArgumentCaptor;

public class PropertyRestControllerUnitTest {

    private final PropertyRestService propertyService = Mockito.mock(PropertyRestService.class);
    private final AuthRestService authRestService = Mockito.mock(AuthRestService.class);
    private final AccommodationReviewRestService reviewService = Mockito.mock(AccommodationReviewRestService.class);

    private final PropertyRestController controller = new PropertyRestController(propertyService, authRestService, reviewService);

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listProperties_asNonOwner_returnsAll() {
        List<PropertySummaryDto> dtoList = List.of(new PropertySummaryDto("P1", "MyProp", 1, 1, "Prov", 1, 2));
        when(propertyService.getAllPropertiesDto()).thenReturn(dtoList);

        // no authentication -> non-owner
        SecurityContextHolder.clearContext();

        ResponseEntity<BaseResponseDto<ArrayList<PropertySummaryDto>>> resp = controller.listProperties(null, null, null);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(1, resp.getBody().getData().size());
        assertTrue(resp.getBody().getMessage().toLowerCase().contains("all properties"));
    }

    @Test
    void listReviewsByProperty_missingPropertyId_returnsBadRequest() {
        ResponseEntity<BaseResponseDto<List<AccommodationReviewDTO>>> resp = controller.listReviewsByProperty(null);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().getMessage().toLowerCase().contains("propertyid is required"));
    }

    @Test
    void listReviewsByProperty_ownerForbidden_whenNotOwner() {
        // setup authentication as owner
        GrantedAuthority ownerAuth = new SimpleGrantedAuthority("ACCOMMODATION_OWNER");
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("ownerUser", "x", List.of(ownerAuth));
        SecurityContextHolder.getContext().setAuthentication(auth);

        // property detail has a different owner id
        PropertyDetailDto dto = new PropertyDetailDto();
        dto.setOwnerId(UUID.randomUUID().toString());
        when(propertyService.getPropertyDetailDto(anyString())).thenReturn(dto);

        // authRestService returns a caller aggregate with a different id
        AccommodationOwner caller = new AccommodationOwner();
        caller.setId(UUID.randomUUID());
        when(authRestService.findAggregateByUsername(anyString())).thenReturn(caller);

        ResponseEntity<BaseResponseDto<List<AccommodationReviewDTO>>> resp = controller.listReviewsByProperty("P-1");
        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
    }

    @Test
    void predictPropertyId_returnsPrediction() {
        UUID ownerUuid = UUID.randomUUID();
        when(propertyService.predictNextPropertySequence()).thenReturn(42);

        ResponseEntity<BaseResponseDto<Map<String, Object>>> resp = controller.predictPropertyId(1, ownerUuid.toString());
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        Map<String, Object> data = resp.getBody().getData();
        assertNotNull(data);
        assertTrue(data.containsKey("predictedPropertyId"));
        assertEquals(42, data.get("nextSequence"));
    }

    @Test
    void listProperties_asOwner_ownerNotFound_returnsNotFound() {
        // setup authentication as owner
        GrantedAuthority ownerAuth = new SimpleGrantedAuthority("ACCOMMODATION_OWNER");
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("ownerUser", "x", List.of(ownerAuth));
        SecurityContextHolder.getContext().setAuthentication(auth);

        // authRestService returns null -> owner not found
        when(authRestService.findAggregateByUsername(anyString())).thenReturn(null);

        ResponseEntity<BaseResponseDto<ArrayList<PropertySummaryDto>>> resp = controller.listProperties(null, null, null);
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().getMessage().toLowerCase().contains("owner not found"));
    }

    @Test
    void createProperty_superadmin_missingOwnerId_returnsBadRequest() {
        GrantedAuthority superAuth = new SimpleGrantedAuthority("SUPERADMIN");
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("superUser", "x", List.of(superAuth));
        SecurityContextHolder.getContext().setAuthentication(auth);

        BaseRequestDto<PropertyCreateRequest> req = new BaseRequestDto<>();
        PropertyCreateRequest pcr = new PropertyCreateRequest();
        // ownerId left null to trigger bad request for superadmin
        req.setData(pcr);

        ResponseEntity<BaseResponseDto<PropertyDetailDto>> resp = controller.createProperty(req);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().getMessage().toLowerCase().contains("superadmin must provide ownerid"));
    }

    @Test
    void createProperty_owner_setsOwnerId_andCallsService() {
        GrantedAuthority ownerAuth = new SimpleGrantedAuthority("ACCOMMODATION_OWNER");
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("ownerUser", "x", List.of(ownerAuth));
        SecurityContextHolder.getContext().setAuthentication(auth);

        // setup returned owner aggregate
        AccommodationOwner owner = new AccommodationOwner();
        UUID ownerId = UUID.randomUUID();
        owner.setId(ownerId);
        when(authRestService.findAggregateByUsername(anyString())).thenReturn(owner);

        BaseRequestDto<PropertyCreateRequest> req = new BaseRequestDto<>();
        PropertyCreateRequest pcr = new PropertyCreateRequest();
        pcr.setOwnerId("some-other-id");
        req.setData(pcr);

        PropertyDetailDto created = new PropertyDetailDto();
        when(propertyService.createProperty(org.mockito.ArgumentMatchers.<PropertyCreateRequest>any())).thenReturn(created);

        ResponseEntity<BaseResponseDto<PropertyDetailDto>> resp = controller.createProperty(req);
        assertEquals(HttpStatus.CREATED, resp.getStatusCode());

        ArgumentCaptor<PropertyCreateRequest> captor = ArgumentCaptor.forClass(PropertyCreateRequest.class);
        verify(propertyService).createProperty(captor.capture());
        PropertyCreateRequest passed = captor.getValue();
        assertEquals(ownerId.toString(), passed.getOwnerId());
    }
}
