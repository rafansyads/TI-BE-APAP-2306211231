package apap.ti._5.accommodation_2306211231_be.restcontroller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import apap.ti._5.accommodation_2306211231_be.restdto.BaseRequestDto;
import apap.ti._5.accommodation_2306211231_be.restdto.request.property.PropertyCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.property.PropertyUpdateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.RoomCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.roomtype.RoomTypeCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.response.property.PropertyDetailDto;
import apap.ti._5.accommodation_2306211231_be.restservice.PropertyRestService;

@ExtendWith(MockitoExtension.class)
public class PropertyRestControllerCrudTests {

    @Mock private PropertyRestService propertyRestService;

    @InjectMocks private PropertyRestController controller;

    private MockMvc mvc;
    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void createPropertySuccessReturns201() throws Exception {
        PropertyDetailDto detail = new PropertyDetailDto();
        detail.setPropertyId("HOT-ABCD-001");
        detail.setTotalRoom(1);
        when(propertyRestService.createProperty(any(PropertyCreateRequest.class))).thenReturn(detail);

        PropertyCreateRequest body = new PropertyCreateRequest();
        body.setPropertyName("X"); body.setType(1); body.setAddress("A"); body.setProvince(31);
        body.setTotalRoom(0); body.setActiveStatus(1); body.setOwnerName("O"); body.setOwnerId(java.util.UUID.randomUUID().toString());
        RoomTypeCreateRequest rt = RoomTypeCreateRequest.builder().name("Type").price(1).capacity(1).floor(1)
          .rooms(java.util.List.of(new RoomCreateRequest(null, null, 1, 1, null, null, null))).build();
        body.setRoomTypes(java.util.List.of(rt));

        BaseRequestDto<PropertyCreateRequest> req = new BaseRequestDto<>();
        req.setData(body);

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/property/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isCreated())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.status", is(201)))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.propertyId", is("HOT-ABCD-001")));
    }

    @Test
    void createPropertyErrorReturns400() throws Exception {
        when(propertyRestService.createProperty(any(PropertyCreateRequest.class))).thenThrow(new IllegalArgumentException("bad"));

        PropertyCreateRequest body = new PropertyCreateRequest();
        body.setPropertyName("X"); body.setType(1); body.setAddress("A"); body.setProvince(31);
        body.setTotalRoom(0); body.setActiveStatus(1); body.setOwnerName("O"); body.setOwnerId(java.util.UUID.randomUUID().toString());
        RoomTypeCreateRequest rt = RoomTypeCreateRequest.builder().name("Type").price(1).capacity(1).floor(1)
          .rooms(java.util.List.of(new RoomCreateRequest(null, null, 1, 1, null, null, null))).build();
        body.setRoomTypes(java.util.List.of(rt));
        BaseRequestDto<PropertyCreateRequest> req = new BaseRequestDto<>();
        req.setData(body);

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/property/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.status", is(400)));
    }

    @Test
    void updatePropertySuccessReturns200() throws Exception {
        PropertyDetailDto detail = new PropertyDetailDto();
        detail.setPropertyId("HOT-ABCD-001");
        when(propertyRestService.updateProperty(eq("HOT-ABCD-001"), any(PropertyUpdateRequest.class))).thenReturn(detail);

        PropertyUpdateRequest body = new PropertyUpdateRequest();
        body.setPropertyId("HOT-ABCD-001"); body.setPropertyName("X"); body.setType(1); body.setAddress("A"); body.setProvince(31);
        body.setTotalRoom(0); body.setActiveStatus(1); body.setOwnerName("O"); body.setOwnerId(java.util.UUID.randomUUID().toString());
        BaseRequestDto<PropertyUpdateRequest> req = new BaseRequestDto<>();
        req.setData(body);

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/property/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.status", is(200)))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.propertyId", is("HOT-ABCD-001")));
    }

    @Test
    void updatePropertyErrorReturns400() throws Exception {
        when(propertyRestService.updateProperty(eq("HOT-ABCD-001"), any(PropertyUpdateRequest.class))).thenThrow(new IllegalArgumentException("bad"));

        PropertyUpdateRequest body = new PropertyUpdateRequest();
        body.setPropertyId("HOT-ABCD-001"); body.setPropertyName("X"); body.setType(1); body.setAddress("A"); body.setProvince(31);
        body.setTotalRoom(0); body.setActiveStatus(1); body.setOwnerName("O"); body.setOwnerId(java.util.UUID.randomUUID().toString());
        BaseRequestDto<PropertyUpdateRequest> req = new BaseRequestDto<>();
        req.setData(body);

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/property/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.status", is(400)));
    }

    @Test
    void updateRoomsSuccessReturns200() throws Exception {
        PropertyDetailDto detail = new PropertyDetailDto();
        detail.setPropertyId("HOT-ABCD-001");
        when(propertyRestService.updatePropertyRooms(eq("HOT-ABCD-001"), any(RoomTypeCreateRequest.class))).thenReturn(detail);

        RoomTypeCreateRequest body = RoomTypeCreateRequest.builder().propertyId("HOT-ABCD-001").name("Type").price(1).capacity(1).floor(1)
          .rooms(java.util.List.of(new RoomCreateRequest(null, null, 1, 1, null, null, null))).build();
        BaseRequestDto<RoomTypeCreateRequest> req = new BaseRequestDto<>();
        req.setData(body);

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/property/updateroom")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.status", is(200)))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.propertyId", is("HOT-ABCD-001")));
    }

    @Test
    void updateRoomsErrorReturns400() throws Exception {
        when(propertyRestService.updatePropertyRooms(eq("HOT-ABCD-001"), any(RoomTypeCreateRequest.class))).thenThrow(new IllegalArgumentException("bad"));

        RoomTypeCreateRequest body = RoomTypeCreateRequest.builder().propertyId("HOT-ABCD-001").name("Type").price(1).capacity(1).floor(1)
          .rooms(java.util.List.of(new RoomCreateRequest(null, null, 1, 1, null, null, null))).build();
        BaseRequestDto<RoomTypeCreateRequest> req = new BaseRequestDto<>();
        req.setData(body);

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/property/updateroom")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.status", is(400)));
    }
}
